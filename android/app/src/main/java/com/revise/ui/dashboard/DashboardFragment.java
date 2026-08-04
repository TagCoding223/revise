package com.revise.ui.dashboard;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
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
import com.revise.R;
import com.revise.model.Topic;

import java.util.ArrayList;
import java.util.List;

public class DashboardFragment extends Fragment {

    public DashboardFragment() {}

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_dashboard, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // --- Theme Toggle Logic ---
        ImageButton btnThemeToggle = view.findViewById(R.id.btnThemeToggle);

        // 1. Check the current active theme
        int currentNightMode = getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        boolean isDarkMode = currentNightMode == Configuration.UI_MODE_NIGHT_YES;

        // 2. Set the appropriate icon
        if (isDarkMode) {
            btnThemeToggle.setImageResource(R.drawable.ic_theme_light); // Show Sun in Dark Mode
        } else {
            btnThemeToggle.setImageResource(R.drawable.ic_theme_dark);  // Show Moon in Light Mode
        }

        // 3. Handle the click event to switch themes
        btnThemeToggle.setOnClickListener(v -> {
            SharedPreferences prefs = requireActivity().getSharedPreferences("ThemePrefs", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();

            if (isDarkMode) {
                // Switch to Light Mode
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                editor.putInt("ThemeMode", AppCompatDelegate.MODE_NIGHT_NO);
            } else {
                // Switch to Dark Mode
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                editor.putInt("ThemeMode", AppCompatDelegate.MODE_NIGHT_YES);
            }

            // Save the preference
            editor.apply();

            // Note: setDefaultNightMode automatically recreates the Activity to apply the new colors instantly.
        });

        // Profile Buttons
        view.findViewById(R.id.btnProfile).setOnClickListener(v -> {
            Navigation.findNavController(view).navigate(R.id.action_dashboardFragment_to_profileFragment);
        });


        // --- Demo Data Setup ---
        List<Topic> todayTopics = new ArrayList<>();
        todayTopics.add(new Topic("1", "Java Class Loader Subsystem", "Workflow and inner workings of the loading phase. Focus on system environment classpath variable overrides.", 3, "today"));
        todayTopics.add(new Topic("2", "DBMS Normalization Forms", "Differences between 3NF, 4NF, and 5NF with practical examples of data anomalies.", 1, "today"));

        List<Topic> tomorrowTopics = new ArrayList<>();
        tomorrowTopics.add(new Topic("3", "Tomcat Servlets & WEB-INF", "Directory structure routing and hidden configuration files behavior when deleted.", 2, "tomorrow"));

        List<Topic> upcomingTopics = new ArrayList<>();
        upcomingTopics.add(new Topic("4", "Algorithm Design Approaches", "Comparison between greedy, dynamic programming, and divide & conquer strategies.", 5, "other"));

        /*
         * Note on Adapter Logic for Active vs Disabled UI:
         * When you bind your RecyclerView adapters later, you will use this logic
         * inside your ViewHolder's bind() method based on the topic's category:
         *
         * if (!topic.getCategory().equals("today")) {
         *     // Fade the entire card to 60% opacity to look disabled
         *     cardContainer.setAlpha(0.6f);
         *
         *     // Disable the Revise button
         *     btnRevise.setEnabled(false);
         *     btnRevise.setBackgroundTintList(ColorStateList.valueOf(Color.GRAY));
         * } else {
         *     // Make it fully opaque and active
         *     cardContainer.setAlpha(1.0f);
         *     btnRevise.setEnabled(true);
         * }
         */

        // Wire the FAB to the Create Modal
        view.findViewById(R.id.fabAddTopic).setOnClickListener(v -> showCreateUpdateModal(null));

        // Example wiring adapter:
//         TopicAdapter adapter = new TopicAdapter(todayTopics, this::showViewModal, this::showCreateUpdateModal, this::showDeleteModal);

        // --- Create the Listener Implementation ---
        TopicAdapter.OnTopicClickListener topicClickListener = new TopicAdapter.OnTopicClickListener() {
            @Override
            public void onViewClick(Topic topic) {
                showViewModal(topic); // Opens View Modal
            }

            @Override
            public void onEditClick(Topic topic) {
                showCreateUpdateModal(topic); // Opens Update Modal with existing data
            }

            @Override
            public void onDeleteClick(Topic topic) {
                showDeleteModal(topic); // Opens Delete Modal
            }

            @Override
            public void onReviseClick(Topic topic) {
                Toast.makeText(getContext(), "Revise clicked for " + topic.getTitle(), Toast.LENGTH_SHORT).show();
                // Later, you will call your API here to update the stage
            }
        };

        // --- Wire up RecyclerViews with the listener ---
        RecyclerView rvToday = view.findViewById(R.id.rvToday);
        rvToday.setLayoutManager(new LinearLayoutManager(getContext()));
        rvToday.setAdapter(new TopicAdapter(todayTopics, topicClickListener)); // Passed listener

        RecyclerView rvTomorrow = view.findViewById(R.id.rvTomorrow);
        rvTomorrow.setLayoutManager(new LinearLayoutManager(getContext()));
        rvTomorrow.setAdapter(new TopicAdapter(tomorrowTopics, topicClickListener)); // Passed listener

        RecyclerView rvUpcoming = view.findViewById(R.id.rvUpcoming);
        rvUpcoming.setLayoutManager(new LinearLayoutManager(getContext()));
        rvUpcoming.setAdapter(new TopicAdapter(upcomingTopics, topicClickListener)); // Passed listener
    }

    // ==========================================
    // 1. CREATE / UPDATE MODAL
    // ==========================================
    private void showCreateUpdateModal(@Nullable Topic existingTopic) {
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_topic_form, null);
        AlertDialog dialog = new MaterialAlertDialogBuilder(requireContext())
                .setView(dialogView)
                .setBackground(new ColorDrawable(Color.TRANSPARENT)) // Allows the rounded corners of XML to show
                .create();

        TextView tvTitle = dialogView.findViewById(R.id.tvFormTitle);
        EditText etTopicTitle = dialogView.findViewById(R.id.etTopicTitle);
        EditText etTopicDesc = dialogView.findViewById(R.id.etTopicDesc);
        LinearLayout layoutLinks = dialogView.findViewById(R.id.layoutLinksContainer);

        // Populate if updating
        if (existingTopic != null) {
            tvTitle.setText("Update Topic");
            etTopicTitle.setText(existingTopic.getTitle());
            etTopicDesc.setText(existingTopic.getDescription());
            // Loop through existing links and add dynamic views...
        } else {
            // Add at least one empty link input by default
            addDynamicLinkView(layoutLinks, "");
        }

        // Add New Link Button
        dialogView.findViewById(R.id.btnAddLink).setOnClickListener(v -> addDynamicLinkView(layoutLinks, ""));

        // Cancel Button
        dialogView.findViewById(R.id.btnCancel).setOnClickListener(v -> dialog.dismiss());

        // Save/Create Button
        dialogView.findViewById(R.id.btnSave).setOnClickListener(v -> {
            String title = etTopicTitle.getText().toString().trim();
            if (title.isEmpty()) {
                Toast.makeText(getContext(), "Title is required", Toast.LENGTH_SHORT).show();
                return;
            }

            // Extract text from dynamic links
            List<String> finalLinks = new ArrayList<>();
            for (int i = 0; i < layoutLinks.getChildCount(); i++) {
                View linkView = layoutLinks.getChildAt(i);
                EditText etLink = linkView.findViewById(R.id.etLinkUrl);
                String linkText = etLink.getText().toString().trim();
                if (!linkText.isEmpty()) {
                    finalLinks.add(linkText);
                }
            }

            if (existingTopic == null) {
                // TODO: Call Axios/Retrofit POST /api/v1/topics
                Toast.makeText(getContext(), "Creating Topic...", Toast.LENGTH_SHORT).show();
            } else {
                // TODO: Call Axios/Retrofit PUT /api/v1/topics/{id}
                Toast.makeText(getContext(), "Updating Topic...", Toast.LENGTH_SHORT).show();
            }
            dialog.dismiss();
        });

        dialog.show();
    }

    // Helper to dynamically inject link inputs
    private void addDynamicLinkView(LinearLayout container, String existingUrl) {
        View linkView = LayoutInflater.from(getContext()).inflate(R.layout.item_dynamic_link, container, false);
        EditText etLink = linkView.findViewById(R.id.etLinkUrl);
        etLink.setText(existingUrl);

        linkView.findViewById(R.id.btnRemoveLink).setOnClickListener(v -> container.removeView(linkView));
        container.addView(linkView);
    }

    // ==========================================
    // 2. VIEW MODAL
    // ==========================================
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

    // ==========================================
    // 3. DELETE CONFIRMATION MODAL
    // ==========================================
    private void showDeleteModal(Topic topic) {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Delete Revision Topic")
                .setMessage("Are you sure you want to delete '" + topic.getTitle() + "'? This action cannot be undone and will reset your spacing cycle for this topic.")
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .setPositiveButton("Delete", (dialog, which) -> {
                    // TODO: Call Axios/Retrofit DELETE /api/v1/topics/{id}
                    Toast.makeText(getContext(), "Topic Deleted", Toast.LENGTH_SHORT).show();
                })
                .show();
        // The standard MaterialAlertDialog automatically inherits your app's rounded corners and colors!
    }
}