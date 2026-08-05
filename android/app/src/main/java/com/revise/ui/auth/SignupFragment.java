package com.revise.ui.auth;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.revise.R;
import com.revise.dto.request.SignupRequest;
import com.revise.dto.response.AuthResponse;
import com.revise.network.AuthApiService;
import com.revise.network.RetrofitClient;
import com.revise.network.TokenManager;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SignupFragment extends Fragment {

    private TextInputEditText etFullName, etEmail, etPassword, etConfirmPassword;
    private MaterialButton btnSignup;
    private AuthApiService apiService;

    public SignupFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_signup, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 1. Initialize Views
        etFullName = view.findViewById(R.id.etFullName);
        etEmail = view.findViewById(R.id.etEmail);
        etPassword = view.findViewById(R.id.etPassword);
        etConfirmPassword = view.findViewById(R.id.etConfirmPassword);
        btnSignup = view.findViewById(R.id.btnSignup);

        // 2. Initialize API Service
        apiService = RetrofitClient.getClient(requireContext()).create(AuthApiService.class);

        // 3. Handle Navigation to Login
        view.findViewById(R.id.tvLoginLink).setOnClickListener(v -> {
            Navigation.findNavController(view).navigate(R.id.action_signupFragment_to_loginFragment);
        });

        // 4. Handle Signup Button Click
        btnSignup.setOnClickListener(v -> attemptSignup(view));
    }

    private void attemptSignup(View view) {
        String fullName = etFullName.getText() != null ? etFullName.getText().toString().trim() : "";
        String email = etEmail.getText() != null ? etEmail.getText().toString().trim() : "";
        String password = etPassword.getText() != null ? etPassword.getText().toString() : "";
        String confirmPassword = etConfirmPassword.getText() != null ? etConfirmPassword.getText().toString() : "";

        // --- Local Validation ---
        if (fullName.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            Toast.makeText(getContext(), "All fields are required", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(getContext(), "Invalid email format", Toast.LENGTH_SHORT).show();
            return;
        }

        if (password.length() < 8) {
            Toast.makeText(getContext(), "Password must be at least 8 characters", Toast.LENGTH_SHORT).show();
            return;
        }

        // Checks for at least one lowercase letter
        if (!password.matches(".*[a-z].*")) {
            Toast.makeText(getContext(),"Password must contain at least one lowercase letter.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Checks for at least one uppercase letter
        if (!password.matches(".*[A-Z].*")) {
            Toast.makeText(getContext(),"Password must contain at least one uppercase letter.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Checks for at least one digit
        if (!password.matches(".*[0-9].*")) {
            Toast.makeText(getContext(),"Password must contain at least one number.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Checks for at least one special symbol (non-alphanumeric)
        if (!password.matches(".*[^a-zA-Z0-9].*")) {
            Toast.makeText(getContext(),"Password must contain at least one special symbol.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!password.equals(confirmPassword)) {
            Toast.makeText(getContext(), "Passwords do not match", Toast.LENGTH_SHORT).show();
            return;
        }

        // Disable button to prevent multiple submissions
        btnSignup.setEnabled(false);
        btnSignup.setText("Creating Account...");

        // --- Execute API Call ---
        SignupRequest request = new SignupRequest(password,email,fullName);

        apiService.signup(request).enqueue(new Callback<AuthResponse>() {
            @Override
            public void onResponse(Call<AuthResponse> call, Response<AuthResponse> response) {
                btnSignup.setEnabled(true);
                btnSignup.setText("Sign Up");

                if (response.isSuccessful() && response.body() != null) {
                    AuthResponse authData = response.body();
                    Toast.makeText(getContext(), "Signup successful! Check your email.", Toast.LENGTH_LONG).show();

                    TokenManager tokenManager = new TokenManager(requireContext());
                    tokenManager.saveTokens(
                            authData.getToken(),
                            authData.getRefreshToken(),
                            authData.getUserId()
                    );

                    // Package the email to pass to the OTP screen
                    Bundle bundle = new Bundle();
                    bundle.putString("USER_EMAIL", email);

                    // Navigate to OTP screen
                    Navigation.findNavController(view).navigate(R.id.action_signupFragment_to_verifyOtpFragment, bundle);
                } else {
                    // Handle 409 Conflict (User already exists) or 400 Bad Request
                    if (response.code() == 409) {
                        Toast.makeText(getContext(), "Account already exists. Please log in.", Toast.LENGTH_LONG).show();
                    } else {
                        Log.d("Signup",response.toString());
                        Toast.makeText(getContext(), "Signup failed. Please try again.", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<AuthResponse> call, Throwable t) {
                btnSignup.setEnabled(true);
                btnSignup.setText("Sign Up");
                Toast.makeText(getContext(), "Network Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}