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