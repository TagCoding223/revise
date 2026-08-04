package com.revise.ui.profile;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.revise.R;

public class ProfileFragment extends Fragment {

    public ProfileFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        TextView tvProfileName = view.findViewById(R.id.tvProfileName);
        TextView tvProfileEmail = view.findViewById(R.id.tvProfileEmail);
        ImageView ivVerifiedBadge = view.findViewById(R.id.ivVerifiedBadge);

        // --- Demo Data (Replace with Retrofit API call later) ---
        String userName = "Student Developer";
        String userEmail = "student@example.com";
        boolean isEmailVerified = true; // Toggle this to test the red cross!

        // Populate UI
        tvProfileName.setText(userName);
        tvProfileEmail.setText(userEmail);

        if (isEmailVerified) {
            ivVerifiedBadge.setImageResource(R.drawable.ic_verified_blue);
        } else {
            ivVerifiedBadge.setImageResource(R.drawable.ic_unverified_red);
        }

        // --- Navigation Actions ---

        // Go back to Dashboard
        view.findViewById(R.id.btnBack).setOnClickListener(v -> {
            Navigation.findNavController(view).popBackStack();
        });

        // Go to Set Password Page
        view.findViewById(R.id.btnChangePassword).setOnClickListener(v -> {
            Navigation.findNavController(view).navigate(R.id.action_profileFragment_to_setPasswordFragment);
        });
    }
}