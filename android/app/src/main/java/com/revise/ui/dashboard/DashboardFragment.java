package com.revise.ui.dashboard;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.revise.MainActivity;
import com.revise.R;
import com.revise.dto.request.TopicRequest;
import com.revise.model.Topic;
import com.revise.network.RetrofitClient;
import com.revise.network.TokenManager;
import com.revise.network.TopicApiService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DashboardFragment extends Fragment {

    private RecyclerView rvToday, rvTomorrow, rvUpcoming;
    private TopicApiService apiService;

    public DashboardFragment() {}

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_dashboard, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize API Service
        apiService = RetrofitClient.getClient(requireContext()).create(TopicApiService.class);

        // --- Theme Toggle Logic ---
        ImageButton btnThemeToggle = view.findViewById(R.id.btnThemeToggle);
        int currentNightMode = getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        boolean isDarkMode = currentNightMode == Configuration.UI_MODE_NIGHT_YES;

        btnThemeToggle.setImageResource(isDarkMode ? R.drawable.ic_theme_light : R.drawable.ic_theme_dark); //[cite: 8]

        btnThemeToggle.setOnClickListener(v -> {
            SharedPreferences prefs = requireActivity().getSharedPreferences("ThemePrefs", Context.MODE_PRIVATE);
            if (isDarkMode) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                prefs.edit().putInt("ThemeMode", AppCompatDelegate.MODE_NIGHT_NO).apply();
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                prefs.edit().putInt("ThemeMode", AppCompatDelegate.MODE_NIGHT_YES).apply();
            }
        });

        // Setup Buttons[cite: 8]
        view.findViewById(R.id.btnProfile).setOnClickListener(v ->
                Navigation.findNavController(view).navigate(R.id.action_dashboardFragment_to_profileFragment));
        view.findViewById(R.id.btnLogout).setOnClickListener(v -> showLogoutConfirmationDialog());
        view.findViewById(R.id.fabAddTopic).setOnClickListener(v -> showCreateUpdateModal(null));

        // Initialize RecyclerViews
        rvToday = view.findViewById(R.id.rvToday);
        rvTomorrow = view.findViewById(R.id.rvTomorrow);
        rvUpcoming = view.findViewById(R.id.rvUpcoming);

        rvToday.setLayoutManager(new LinearLayoutManager(getContext()));
        rvTomorrow.setLayoutManager(new LinearLayoutManager(getContext()));
        rvUpcoming.setLayoutManager(new LinearLayoutManager(getContext()));

        // Fetch live data from backend
        fetchTopics();
    }

    // ==========================================
    // 1. DATA FETCHING
    // ==========================================
    private void fetchTopics() {
        // Now calling getAllTopics()
        apiService.getAllTopics().enqueue(new Callback<List<Topic>>() {
            @Override
            public void onResponse(Call<List<Topic>> call, Response<List<Topic>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Topic> allTopics = response.body();

                    List<Topic> today = new ArrayList<>();
                    List<Topic> tomorrow = new ArrayList<>();
                    List<Topic> upcoming = new ArrayList<>();

                    // Manually group them based on the backend's dynamic category string
                    for (Topic topic : allTopics) {
                        if ("today".equals(topic.getCategory())) {
                            today.add(topic);
                        } else if ("tomorrow".equals(topic.getCategory())) {
                            tomorrow.add(topic);
                        } else {
                            upcoming.add(topic);
                        }
                    }

                    if (allTopics.isEmpty()) {
                        Toast.makeText(getContext(), "No topics found. Start creating!", Toast.LENGTH_LONG).show();
                    }

                    TopicAdapter.OnTopicClickListener listener = createTopicClickListener();
                    rvToday.setAdapter(new TopicAdapter(today, listener));
                    rvTomorrow.setAdapter(new TopicAdapter(tomorrow, listener));
                    rvUpcoming.setAdapter(new TopicAdapter(upcoming, listener));

                } else {
                    handleServerError(response.code());
                }
            }

            @Override
            public void onFailure(Call<List<Topic>> call, Throwable t) {
                handleNetworkError(t);
            }
        });
    }

    private TopicAdapter.OnTopicClickListener createTopicClickListener() {
        return new TopicAdapter.OnTopicClickListener() {
            @Override
            public void onViewClick(Topic topic) { showViewModal(topic); }
            @Override
            public void onEditClick(Topic topic) { showCreateUpdateModal(topic); }
            @Override
            public void onDeleteClick(Topic topic) { showDeleteModal(topic); }
            @Override
            public void onReviseClick(Topic topic) { executeRevise(topic); }
        };
    }

    // ==========================================
    // 2. CRUD EXECUTIONS
    // ==========================================
    private void executeRevise(Topic topic) {
        apiService.reviseTopic(topic.getId()).enqueue(new Callback<Topic>() {
            @Override
            public void onResponse(Call<Topic> call, Response<Topic> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(getContext(), "Revision logged!", Toast.LENGTH_SHORT).show();
                    fetchTopics(); // Refresh dashboard
                } else {
                    handleServerError(response.code());
                }
            }
            @Override
            public void onFailure(Call<Topic> call, Throwable t) { handleNetworkError(t); }
        });
    }

    private void executeDelete(Topic topic) {
        apiService.deleteTopic(topic.getId()).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(getContext(), "Topic Deleted", Toast.LENGTH_SHORT).show();
                    fetchTopics(); // Refresh dashboard
                } else {
                    handleServerError(response.code());
                }
            }
            @Override
            public void onFailure(Call<Void> call, Throwable t) { handleNetworkError(t); }
        });
    }

    private void saveOrUpdateTopic(Topic existingTopic, String title, String description, List<String> links, AlertDialog dialog) {
        TopicRequest payload = new TopicRequest(title,description,links);
        Call<Topic> call = (existingTopic == null)
                ? apiService.createTopic(payload)
                : apiService.updateTopic(existingTopic.getId(), payload);

        call.enqueue(new Callback<Topic>() {
            @Override
            public void onResponse(Call<Topic> call, Response<Topic> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(getContext(), "Saved successfully!", Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                    fetchTopics();
                } else {
                    handleServerError(response.code());
                }
            }
            @Override
            public void onFailure(Call<Topic> call, Throwable t) { handleNetworkError(t); }
        });
    }

    // ==========================================
    // 3. ERROR HANDLING HELPERS
    // ==========================================
    private void handleServerError(int code) {
        if (code >= 500) {
            Toast.makeText(getContext(), "Server error. Please try again later.", Toast.LENGTH_LONG).show();
        } else if (code == 404) {
            Toast.makeText(getContext(), "Topic not found. It may have been deleted.", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(getContext(), "Something went wrong (Code: " + code + ")", Toast.LENGTH_SHORT).show();
        }
    }

    private void handleNetworkError(Throwable t) {
        if (t instanceof IOException) {
            Toast.makeText(getContext(), "Network unreachable. Check your connection.", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(getContext(), "An unexpected error occurred.", Toast.LENGTH_SHORT).show();
        }
    }

    // ==========================================
    // 4. UI MODALS
    // ==========================================
    private void showCreateUpdateModal(@Nullable Topic existingTopic) {
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_topic_form, null);
        AlertDialog dialog = new MaterialAlertDialogBuilder(requireContext())
                .setView(dialogView)
                .setBackground(new ColorDrawable(Color.TRANSPARENT))
                .create();

        TextView tvTitle = dialogView.findViewById(R.id.tvFormTitle);
        EditText etTopicTitle = dialogView.findViewById(R.id.etTopicTitle);
        EditText etTopicDesc = dialogView.findViewById(R.id.etTopicDesc);
        LinearLayout layoutLinks = dialogView.findViewById(R.id.layoutLinksContainer);

        if (existingTopic != null) {
            tvTitle.setText("Update Topic");
            etTopicTitle.setText(existingTopic.getTitle());
            etTopicDesc.setText(existingTopic.getDescription());

            // NEW: Populate existing links if they exist
            if (existingTopic.getLinks() != null && !existingTopic.getLinks().isEmpty()) {
                for (String link : existingTopic.getLinks()) {
                    addDynamicLinkView(layoutLinks, link);
                }
            } else {
                addDynamicLinkView(layoutLinks, ""); // Show one empty box if no links
            }
        } else {
            addDynamicLinkView(layoutLinks, "");
        }

        dialogView.findViewById(R.id.btnAddLink).setOnClickListener(v -> addDynamicLinkView(layoutLinks, ""));
        dialogView.findViewById(R.id.btnCancel).setOnClickListener(v -> dialog.dismiss());

        dialogView.findViewById(R.id.btnSave).setOnClickListener(v -> {
            String title = etTopicTitle.getText().toString().trim();
            if (title.isEmpty()) {
                Toast.makeText(getContext(), "Title is required", Toast.LENGTH_SHORT).show();
                return;
            }

            // Extract and Validate text from dynamic links
            List<String> finalLinks = new ArrayList<>();
            for (int i = 0; i < layoutLinks.getChildCount(); i++) {
                View linkView = layoutLinks.getChildAt(i);
                EditText etLink = linkView.findViewById(R.id.etLinkUrl);
                String linkText = etLink.getText().toString().trim();

                if (!linkText.isEmpty()) {
                    // Strict Client-Side URL Validation
                    if (!android.util.Patterns.WEB_URL.matcher(linkText).matches()) {
                        Toast.makeText(getContext(), "Invalid link: " + linkText + "\nPlease enter a valid URL (e.g., https://...)", Toast.LENGTH_LONG).show();
                        return; // Stop the save process entirely
                    }
                    finalLinks.add(linkText);
                }
            }

            saveOrUpdateTopic(existingTopic, title, etTopicDesc.getText().toString().trim(), finalLinks, dialog);
        });

        dialog.show();
    }

    private void addDynamicLinkView(LinearLayout container, String existingUrl) {
        View linkView = LayoutInflater.from(getContext()).inflate(R.layout.item_dynamic_link, container, false);
        ((EditText) linkView.findViewById(R.id.etLinkUrl)).setText(existingUrl);
        linkView.findViewById(R.id.btnRemoveLink).setOnClickListener(v -> container.removeView(linkView));
        container.addView(linkView);
    }

    private void showViewModal(Topic topic) {
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_view_topic, null);
        AlertDialog dialog = new MaterialAlertDialogBuilder(requireContext())
                .setView(dialogView)
                .setBackground(new ColorDrawable(Color.TRANSPARENT))
                .create();

        ((TextView) dialogView.findViewById(R.id.tvViewTitle)).setText(topic.getTitle());
        ((TextView) dialogView.findViewById(R.id.tvViewStage)).setText("Stage " + topic.getStage());

        TextView tvDesc = dialogView.findViewById(R.id.tvViewDesc);
        if (topic.getDescription() == null || topic.getDescription().isEmpty()) {
            tvDesc.setText("No description provided.");
            tvDesc.setTextColor(Color.GRAY);
        } else {
            tvDesc.setText(topic.getDescription());
        }

        dialogView.findViewById(R.id.btnCloseView).setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    private void showDeleteModal(Topic topic) {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Delete Revision Topic")
                .setMessage("Are you sure you want to delete '" + topic.getTitle() + "'? This action cannot be undone and will reset your spacing cycle for this topic.") //[cite: 8]
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .setPositiveButton("Delete", (dialog, which) -> executeDelete(topic))
                .show();
    }

    private void showLogoutConfirmationDialog() {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Log Out")
                .setMessage("Are you sure you want to log out of your account?") //[cite: 8]
                .setPositiveButton("Log Out", (dialog, which) -> executeLogout())
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void executeLogout() {
        new TokenManager(requireContext()).clearTokens();
        Intent intent = new Intent(requireActivity(), MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }
}