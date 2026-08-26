package com.revise.ui.dashboard;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
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
import com.revise.network.RetrofitClient;
import com.revise.network.TokenManager;
import com.revise.network.TopicApiService;
import com.revise.ui.dashboard.dialogs.ActionDialogHelper;
import com.revise.ui.dashboard.dialogs.TopicFormDialog;
import com.revise.ui.dashboard.dialogs.TopicViewDialog;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DashboardFragment extends Fragment {

    private RecyclerView rvToday, rvTomorrow, rvUpcoming;
    private TextView tvEmptyToday, tvEmptyTomorrow, tvEmptyUpcoming;
    private TopicApiService apiService;

    public DashboardFragment() {}

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_dashboard, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        apiService = RetrofitClient.getClient(requireContext()).create(TopicApiService.class);

        setupThemeToggle(view);
        setupButtons(view);
        setupRecyclerViews(view);

        fetchTopics();
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
    // DATA FETCHING & GROUPING
    // ==========================================
    private void fetchTopics() {
        apiService.getAllTopics().enqueue(new Callback<List<Topic>>() {
            @Override
            public void onResponse(Call<List<Topic>> call, Response<List<Topic>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Topic> allTopics = response.body();
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
                } else {
                    handleServerError(response.code());
                }
            }
            @Override
            public void onFailure(Call<List<Topic>> call, Throwable t) { handleNetworkError(t); }
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
    // API EXECUTIONS
    // ==========================================
    private void saveOrUpdateTopic(TopicRequest request, @Nullable Topic existingTopic) {
        Call<Topic> call = (existingTopic == null)
                ? apiService.createTopic(request)
                : apiService.updateTopic(existingTopic.getId(), request);

        call.enqueue(new Callback<Topic>() {
            @Override
            public void onResponse(Call<Topic> call, Response<Topic> response) {
                if (response.isSuccessful()) fetchTopics();
                else handleServerError(response.code());
            }
            @Override
            public void onFailure(Call<Topic> call, Throwable t) { handleNetworkError(t); }
        });
    }

    private void executeDelete(Topic topic) {
        apiService.deleteTopic(topic.getId()).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) fetchTopics();
                else handleServerError(response.code());
            }
            @Override
            public void onFailure(Call<Void> call, Throwable t) { handleNetworkError(t); }
        });
    }

    private void executeRevise(Topic topic) {
        apiService.reviseTopic(topic.getId()).enqueue(new Callback<Topic>() {
            @Override
            public void onResponse(Call<Topic> call, Response<Topic> response) {
                if (response.isSuccessful()) fetchTopics();
                else handleServerError(response.code());
            }
            @Override
            public void onFailure(Call<Topic> call, Throwable t) { handleNetworkError(t); }
        });
    }

    private void executeLogout() {
        new TokenManager(requireContext()).clearTokens();
        // Clear Profile Cache on logout
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

    private void handleServerError(int code) {
        if (code >= 500) Toast.makeText(getContext(), "Server error. Please try again later.", Toast.LENGTH_LONG).show();
        else if (code == 404) Toast.makeText(getContext(), "Topic not found.", Toast.LENGTH_LONG).show();
        else Toast.makeText(getContext(), "Something went wrong (Code: " + code + ")", Toast.LENGTH_SHORT).show();
    }

    private void handleNetworkError(Throwable t) {
        if (t instanceof IOException) Toast.makeText(getContext(), "Network unreachable.", Toast.LENGTH_LONG).show();
        else Toast.makeText(getContext(), "An unexpected error occurred.", Toast.LENGTH_SHORT).show();
    }
}