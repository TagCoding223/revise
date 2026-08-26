package com.revise.ui.auth;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.revise.BuildConfig;
import com.revise.R;
import com.revise.dto.request.GoogleAuthRequest;
import com.revise.dto.request.LoginRequest;
import com.revise.dto.response.AuthResponse;
import com.revise.network.AuthApiService;
import com.revise.network.RetrofitClient;
import com.revise.network.TokenManager;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginFragment extends Fragment {

    private TextInputEditText etEmail, etPassword;
    private MaterialButton btnLogin;
    private AuthApiService apiService;
    private GoogleSignInClient mGoogleSignInClient;

    public LoginFragment() {}

    // --- Google Auth Result Handler ---
    private final ActivityResultLauncher<Intent> googleSignInLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == getActivity().RESULT_OK) {
                    Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(result.getData());
                    try {
                        GoogleSignInAccount account = task.getResult(ApiException.class);
                        String idToken = account.getIdToken();
                        // Send the token to Spring Boot
                        authenticateWithBackend(idToken, getView());
                    } catch (ApiException e) {
                        Log.e("GoogleAuth", "Google sign in failed", e);
                        Toast.makeText(getContext(), "Google Sign-In Failed", Toast.LENGTH_SHORT).show();
                    }
                }
            }
    );

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_login, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        etEmail = view.findViewById(R.id.etEmail);
        etPassword = view.findViewById(R.id.etPassword);
        btnLogin = view.findViewById(R.id.btnLogin);

        // Configure Google Sign-In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(BuildConfig.GOOGLE_WEB_CLIENT_ID)
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(requireActivity(), gso);

        // Wire up the Google Button
        view.findViewById(R.id.btnGoogle).setOnClickListener(v -> {
            mGoogleSignInClient.signOut(); // Ensure prompt shows every time for testing
            Intent signInIntent = mGoogleSignInClient.getSignInIntent();
            googleSignInLauncher.launch(signInIntent);
        });

        // Initialize API Service
        apiService = RetrofitClient.getClient(requireContext()).create(AuthApiService.class);

        view.findViewById(R.id.tvSignupLink).setOnClickListener(v ->
                Navigation.findNavController(view).navigate(R.id.action_loginFragment_to_signupFragment)
        );

        view.findViewById(R.id.btnLogin).setOnClickListener(v -> attemptLogin(view));
    }

    // --- Send Google Token to Backend ---
    private void authenticateWithBackend(String idToken, View view) {
        GoogleAuthRequest request = new GoogleAuthRequest(idToken);

        apiService.googleLogin(request).enqueue(new Callback<AuthResponse>() {
            @Override
            public void onResponse(Call<AuthResponse> call, Response<AuthResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    AuthResponse authData = response.body();

                    TokenManager tokenManager = new TokenManager(requireContext());
                    tokenManager.saveTokens(
                            authData.getToken(),
                            authData.getRefreshToken(),
                            authData.getUserId()
                    );

                    Toast.makeText(getContext(), "Welcome!", Toast.LENGTH_SHORT).show();
                    Navigation.findNavController(view).navigate(R.id.action_loginFragment_to_dashboardFragment);
                } else {
                    Toast.makeText(getContext(), "Backend Authentication Failed", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<AuthResponse> call, Throwable t) {
                Toast.makeText(getContext(), "Network Error", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void attemptLogin(View view) {
        String email = etEmail.getText() != null ? etEmail.getText().toString().trim() : "";
        String password = etPassword.getText() != null ? etPassword.getText().toString().trim() : "";

        // 1. Local Form Validation
        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(getContext(), "All fields are required", Toast.LENGTH_SHORT).show();
            return;
        }

        // TODO: Disable button/show loading spinner here if desired
        btnLogin.setEnabled(false);
        btnLogin.setText("Please Wait...");

        // 2. Prepare the DTO
        LoginRequest request = new LoginRequest(email, password);

        // 3. Execute Async Network Call
        apiService.login(request).enqueue(new Callback<AuthResponse>() {

            @Override
            public void onResponse(Call<AuthResponse> call, Response<AuthResponse> response) {
                btnLogin.setEnabled(true);
                btnLogin.setText("Continue");
                if (response.isSuccessful() && response.body() != null) {
                    AuthResponse authData = response.body();

                    Toast.makeText(getContext(), "Welcome back!", Toast.LENGTH_SHORT).show();

                    // Save the JWT token (authData.getToken()) securely
                    // Initialize TokenManger
                    TokenManager tokenManager = new TokenManager(requireContext());

                    // Save the tokens
                    // we will pass a placeholder string "dummy_refresh_token" for now.
                    tokenManager.saveTokens(
                            authData.getToken(),
                            authData.getRefreshToken(),
                            authData.getUserId()
                    );

                    Toast.makeText(getContext(), "Welcome back!", Toast.LENGTH_SHORT).show();

                    // Navigate to Dashboard
                    Navigation.findNavController(view).navigate(R.id.action_loginFragment_to_dashboardFragment);
                } else {
                    // Handle 401 Unauthorized or 403 Forbidden
                    if (response.code() == 403) {
                        Toast.makeText(getContext(), "Email unverified. Redirecting...", Toast.LENGTH_LONG).show();

                        // TODO: Navigate to OTP Fragment
                        Navigation.findNavController(view).navigate(R.id.action_loginFragment_to_verifyOtpFragment);

                    } else {
                        Toast.makeText(getContext(), "Login Failed: Invalid credentials", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<AuthResponse> call, Throwable t) {
                btnLogin.setEnabled(true);
                btnLogin.setText("Continue");
                // Handles no internet or server down
                Toast.makeText(getContext(), "Network Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}