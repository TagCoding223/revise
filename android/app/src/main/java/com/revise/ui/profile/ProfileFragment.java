package com.revise.ui.profile;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.revise.R;
import com.revise.dto.response.ProfileResponse;
import com.revise.network.AuthApiService;
import com.revise.network.RetrofitClient;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfileFragment extends Fragment {

    private TextView tvProfileName;
    private TextView tvProfileEmail;
    private ImageView ivVerifiedBadge;
    private AuthApiService apiService;

    public ProfileFragment() {}

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 1. Initialize UI Elements
        tvProfileName = view.findViewById(R.id.tvProfileName);
        tvProfileEmail = view.findViewById(R.id.tvProfileEmail);
        ivVerifiedBadge = view.findViewById(R.id.ivVerifiedBadge);

        // Show loading state while network request completes
        tvProfileName.setText("Loading...");
        tvProfileEmail.setText("Fetching details...");

        // 2. Initialize API Service
        apiService = RetrofitClient.getClient(requireContext()).create(AuthApiService.class);

        // 3. Check profile data on cache
        SharedPreferences prefs = requireContext().getSharedPreferences("ProfileCache", Context.MODE_PRIVATE);
        String cachedName = prefs.getString("name", null);

        if (cachedName != null) {
            // Load from Cache
            tvProfileName.setText(cachedName);
            tvProfileEmail.setText(prefs.getString("email", ""));
            boolean isVerified = prefs.getBoolean("verified", false);
            ivVerifiedBadge.setImageResource(isVerified ? R.drawable.ic_verified_blue : R.drawable.ic_unverified_red);
        } else {
            // First time visit: Fetch from Network
            fetchUserProfile();
        }

        // 4. Navigation Actions[cite: 3]
        view.findViewById(R.id.btnBack).setOnClickListener(v -> {
            Navigation.findNavController(view).popBackStack();
        });

        view.findViewById(R.id.btnChangePassword).setOnClickListener(v -> {
            Navigation.findNavController(view).navigate(R.id.action_profileFragment_to_setPasswordFragment);
        });
    }

    private void fetchUserProfile() {
        apiService.getUserProfile().enqueue(new Callback<ProfileResponse>() {
            @Override
            public void onResponse(Call<ProfileResponse> call, Response<ProfileResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ProfileResponse profileData = response.body();

                    // Update UI
                    tvProfileName.setText(profileData.getFullName());
                    tvProfileEmail.setText(profileData.getEmail());
                    ivVerifiedBadge.setImageResource(profileData.isEmailVerified() ? R.drawable.ic_verified_blue : R.drawable.ic_unverified_red);

                    // Save to Cache for next time
                    SharedPreferences prefs = requireContext().getSharedPreferences("ProfileCache", Context.MODE_PRIVATE);
                    prefs.edit()
                            .putString("name", profileData.getFullName())
                            .putString("email", profileData.getEmail())
                            .putBoolean("verified", profileData.isEmailVerified())
                            .apply();

                } else {
                    Log.d("Profile",response.toString());
                    Toast.makeText(getContext(), "Failed to load profile data", Toast.LENGTH_SHORT).show();
                    tvProfileName.setText("Error");
                    tvProfileEmail.setText("");
                }
            }

            @Override
            public void onFailure(Call<ProfileResponse> call, Throwable t) {
                Toast.makeText(getContext(), "Network Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                tvProfileName.setText("Network Error");
                tvProfileEmail.setText("");
            }
        });
    }
}