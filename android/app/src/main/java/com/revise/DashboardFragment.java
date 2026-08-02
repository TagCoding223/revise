package com.revise;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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

        // Header Buttons
        view.findViewById(R.id.btnProfile).setOnClickListener(v -> {
            // Navigate to Profile Fragment (To be created)
        });

        view.findViewById(R.id.btnThemeToggle).setOnClickListener(v -> {
            Toast.makeText(getContext(), "Theme toggle clicked", Toast.LENGTH_SHORT).show();
            // Handle Android DayNight theme switching here
        });

        // Add New Topic Button
        view.findViewById(R.id.fabAddTopic).setOnClickListener(v -> {
            Toast.makeText(getContext(), "Open Create Topic Modal", Toast.LENGTH_SHORT).show();
        });

        // --- Demo Data Setup ---
        List<Topic> todayTopics = new ArrayList<>();
        todayTopics.add(new Topic("1", "Java Class Loader Subsystem", "Workflow and inner workings of the loading phase. Focus on system environment classpath variable overrides.", 3, "today"));
        todayTopics.add(new Topic("2", "DBMS Normalization Forms", "Differences between 3NF, 4NF, and 5NF with practical examples of data anomalies.", 1, "today"));

        List<Topic> tomorrowTopics = new ArrayList<>();
        tomorrowTopics.add(new Topic("3", "Tomcat Servlets & WEB-INF", "Directory structure routing and hidden configuration files behavior when deleted.", 2, "tomorrow"));

        List<Topic> upcomingTopics = new ArrayList<>();
        upcomingTopics.add(new Topic("4", "Algorithm Design Approaches", "Comparison between greedy, dynamic programming, and divide & conquer strategies.", 5, "other"));

        // --- Wire up RecyclerViews ---
        RecyclerView rvToday = view.findViewById(R.id.rvToday);
        rvToday.setLayoutManager(new LinearLayoutManager(getContext()));
        rvToday.setAdapter(new TopicAdapter(todayTopics));

        RecyclerView rvTomorrow = view.findViewById(R.id.rvTomorrow);
        rvTomorrow.setLayoutManager(new LinearLayoutManager(getContext()));
        rvTomorrow.setAdapter(new TopicAdapter(tomorrowTopics));

        RecyclerView rvUpcoming = view.findViewById(R.id.rvUpcoming);
        rvUpcoming.setLayoutManager(new LinearLayoutManager(getContext()));
        rvUpcoming.setAdapter(new TopicAdapter(upcomingTopics));

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
    }
}