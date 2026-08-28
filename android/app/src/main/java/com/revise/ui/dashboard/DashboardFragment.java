package com.revise.ui.dashboard;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.revise.MainActivity;
import com.revise.R;
import com.revise.dto.request.TopicRequest;
import com.revise.model.Topic;
import com.revise.network.TokenManager;
import com.revise.repository.TopicRepository;
import com.revise.ui.dashboard.dialogs.ActionDialogHelper;
import com.revise.ui.dashboard.dialogs.TopicFormDialog;
import com.revise.ui.dashboard.dialogs.TopicViewDialog;

import java.util.ArrayList;
import java.util.List;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
public class DashboardFragment extends Fragment {

    private RecyclerView rvToday, rvTomorrow, rvUpcoming;
    private TextView tvEmptyToday, tvEmptyTomorrow, tvEmptyUpcoming;
    private SwipeRefreshLayout swipeRefreshLayout;
    private TopicRepository repository;

    public DashboardFragment() {}

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_dashboard, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        TokenManager tokenManager = new TokenManager(requireContext());
        if (tokenManager.getAccessToken() == null || tokenManager.getAccessToken().isEmpty()) {
            // Eject them instantly if they somehow got here without a token
            Navigation.findNavController(view).navigate(R.id.loginFragment);
            return;
        }

        checkNotificationPermission();

        // Initialize the Repository
        repository = new TopicRepository(requireContext());

        // Initialize and configure the swipe listener
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);
        swipeRefreshLayout.setOnRefreshListener(() -> {
            // When the user pulls down, manually trigger the repository sync
            fetchTopics();
        });

        setupThemeToggle(view);
        setupButtons(view);
        setupRecyclerViews(view);

        fetchTopics();
        scheduleDailyReminder();
    }

    private void setupButtons(View view) {
        view.findViewById(R.id.btnProfile).setOnClickListener(v ->
                Navigation.findNavController(view).navigate(R.id.action_dashboardFragment_to_profileFragment));

        view.findViewById(R.id.btnLogout).setOnClickListener(v ->
                ActionDialogHelper.showLogoutConfirmation(requireContext(), this::executeLogout));

        view.findViewById(R.id.fabAddTopic).setOnClickListener(v -> openTopicFormModal(null));
    }

    private void setupRecyclerViews(View view) {
        rvToday = view.findViewById(R.id.rvToday);
        rvTomorrow = view.findViewById(R.id.rvTomorrow);
        rvUpcoming = view.findViewById(R.id.rvUpcoming);

        tvEmptyToday = view.findViewById(R.id.tvEmptyToday);
        tvEmptyTomorrow = view.findViewById(R.id.tvEmptyTomorrow);
        tvEmptyUpcoming = view.findViewById(R.id.tvEmptyUpcoming);

        rvToday.setLayoutManager(new LinearLayoutManager(getContext()));
        rvTomorrow.setLayoutManager(new LinearLayoutManager(getContext()));
        rvUpcoming.setLayoutManager(new LinearLayoutManager(getContext()));
    }

    // ==========================================
    // DATA FETCHING & GROUPING (Offline First)
    // ==========================================
    private void fetchTopics() {
        repository.getTopics(new TopicRepository.RepositoryCallback<List<Topic>>() {
            @Override
            public void onSuccess(List<Topic> allTopics) {
                // Stop the spinning animation
                swipeRefreshLayout.setRefreshing(false);

                List<Topic> today = new ArrayList<>();
                List<Topic> tomorrow = new ArrayList<>();
                List<Topic> upcoming = new ArrayList<>();

                for (Topic topic : allTopics) {
                    if ("today".equals(topic.getCategory())) today.add(topic);
                    else if ("tomorrow".equals(topic.getCategory())) tomorrow.add(topic);
                    else upcoming.add(topic);
                }

                updateListState(rvToday, tvEmptyToday, today);
                updateListState(rvTomorrow, tvEmptyTomorrow, tomorrow);
                updateListState(rvUpcoming, tvEmptyUpcoming, upcoming);

                TopicAdapter.OnTopicClickListener listener = createTopicClickListener();
                rvToday.setAdapter(new TopicAdapter(today, listener));
                rvTomorrow.setAdapter(new TopicAdapter(tomorrow, listener));
                rvUpcoming.setAdapter(new TopicAdapter(upcoming, listener));
            }

            @Override
            public void onError(String message) {
                // Stop the spinning animation even if it fails
                swipeRefreshLayout.setRefreshing(false);
                Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateListState(RecyclerView rv, TextView emptyState, List<Topic> data) {
        rv.setVisibility(data.isEmpty() ? View.GONE : View.VISIBLE);
        emptyState.setVisibility(data.isEmpty() ? View.VISIBLE : View.GONE);
    }

    // ==========================================
    // EVENT LISTENERS & MODAL TRIGGERS
    // ==========================================
    private TopicAdapter.OnTopicClickListener createTopicClickListener() {
        return new TopicAdapter.OnTopicClickListener() {
            @Override
            public void onViewClick(Topic topic) {
                new TopicViewDialog(topic).show(getChildFragmentManager(), "ViewDialog");
            }

            @Override
            public void onEditClick(Topic topic) {
                openTopicFormModal(topic);
            }

            @Override
            public void onDeleteClick(Topic topic) {
                ActionDialogHelper.showDeleteConfirmation(requireContext(), topic.getTitle(), () -> executeDelete(topic));
            }

            @Override
            public void onReviseClick(Topic topic) {
                executeRevise(topic);
            }
        };
    }

    private void openTopicFormModal(@Nullable Topic existingTopic) {
        new TopicFormDialog(existingTopic, (request, topic) -> saveOrUpdateTopic(request, topic))
                .show(getChildFragmentManager(), "FormDialog");
    }

    // ==========================================
    // API EXECUTIONS (Via Repository)
    // ==========================================
    private void saveOrUpdateTopic(TopicRequest request, @Nullable Topic existingTopic) {
        TopicRepository.RepositoryCallback<Topic> callback = new TopicRepository.RepositoryCallback<Topic>() {
            @Override
            public void onSuccess(Topic data) {
                Toast.makeText(getContext(), "Saved successfully!", Toast.LENGTH_SHORT).show();
                fetchTopics();
            }
            @Override
            public void onError(String message) {
                Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
            }
        };

        if (existingTopic == null) {
            repository.createTopic(request, callback);
        } else {
            // Pass the existingTopic object directly
            repository.updateTopic(existingTopic, request, callback);
        }
    }

    private void executeDelete(Topic topic) {
        repository.deleteTopic(topic.getId(), new TopicRepository.RepositoryCallback<Void>() {
            @Override
            public void onSuccess(Void data) {
                fetchTopics();
            }

            @Override
            public void onError(String message) {
                Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void executeRevise(Topic topic) {
        repository.reviseTopic(topic, new TopicRepository.RepositoryCallback<Topic>() {
            @Override
            public void onSuccess(Topic data) {
                Toast.makeText(getContext(), "Revision logged!", Toast.LENGTH_SHORT).show();
                fetchTopics();
            }

            @Override
            public void onError(String message) {
                Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void executeLogout() {
        new TokenManager(requireContext()).clearTokens();
        requireContext().getSharedPreferences("ProfileCache", Context.MODE_PRIVATE).edit().clear().apply();

        Intent intent = new Intent(requireActivity(), MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    // ==========================================
    // UTILITIES
    // ==========================================
    private void setupThemeToggle(View view) {
        ImageButton btnThemeToggle = view.findViewById(R.id.btnThemeToggle);
        boolean isDarkMode = (getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES;
        btnThemeToggle.setImageResource(isDarkMode ? R.drawable.ic_theme_light : R.drawable.ic_theme_dark);

        btnThemeToggle.setOnClickListener(v -> {
            SharedPreferences prefs = requireActivity().getSharedPreferences("ThemePrefs", Context.MODE_PRIVATE);
            AppCompatDelegate.setDefaultNightMode(isDarkMode ? AppCompatDelegate.MODE_NIGHT_NO : AppCompatDelegate.MODE_NIGHT_YES);
            prefs.edit().putInt("ThemeMode", isDarkMode ? AppCompatDelegate.MODE_NIGHT_NO : AppCompatDelegate.MODE_NIGHT_YES).apply();
        });
    }

    // For Notification
    private void scheduleDailyReminder() {
        // FOR TESTING: Force a One-Time Test in Code
        // we want to trigger it instantly every time we open the app for testing purposes
//        {
//            androidx.work.OneTimeWorkRequest testRequest =
//                    new androidx.work.OneTimeWorkRequest.Builder(com.revise.workers.RevisionNotificationWorker.class).build();
//            androidx.work.WorkManager.getInstance(requireContext()).enqueue(testRequest);
//        }

        androidx.work.PeriodicWorkRequest revisionWorkRequest =
                new androidx.work.PeriodicWorkRequest.Builder(
                        com.revise.workers.RevisionNotificationWorker.class,
                        24, java.util.concurrent.TimeUnit.HOURS)
                        .build();

        androidx.work.WorkManager.getInstance(requireContext())
                .enqueueUniquePeriodicWork(
                        "DailyRevisionReminder",
                        androidx.work.ExistingPeriodicWorkPolicy.KEEP,
                        revisionWorkRequest
                );
    }
    private final androidx.activity.result.ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new androidx.activity.result.contract.ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    scheduleDailyReminder();
                } else {
                    Toast.makeText(getContext(), "Notifications disabled. You won't receive daily revision alerts.", Toast.LENGTH_LONG).show();
                }
            });

    private void checkNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (androidx.core.content.ContextCompat.checkSelfPermission(
                    requireContext(), android.Manifest.permission.POST_NOTIFICATIONS) ==
                    android.content.pm.PackageManager.PERMISSION_GRANTED) {
                scheduleDailyReminder();
            } else if (shouldShowRequestPermissionRationale(android.Manifest.permission.POST_NOTIFICATIONS)) {
                // Optional: Explain why notifications are helpful
                requestPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS);
            } else {
                requestPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS);
            }
        } else {
            scheduleDailyReminder();
        }
    }
}