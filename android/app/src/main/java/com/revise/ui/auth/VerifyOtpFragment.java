package com.revise.ui.auth;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavOptions;
import androidx.navigation.Navigation;

import com.google.android.material.button.MaterialButton;
import com.revise.R;
import com.revise.dto.response.AuthResponse;
import com.revise.network.AuthApiService;
import com.revise.network.RetrofitClient;
import com.revise.network.TokenManager;

import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class VerifyOtpFragment extends Fragment {

    private EditText etOtp1, etOtp2, etOtp3, etOtp4;
    private MaterialButton btnVerify;
    private TextView tvTimer, tvResend;
    private CountDownTimer countDownTimer;
    private AuthApiService apiService;
    private String userEmail = "";

    // 130 seconds = 2 minutes and 10 seconds
    private static final long START_TIME_IN_MILLIS = 130000;

    public VerifyOtpFragment() {}

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_verify_otp, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        etOtp1 = view.findViewById(R.id.etOtp1);
        etOtp2 = view.findViewById(R.id.etOtp2);
        etOtp3 = view.findViewById(R.id.etOtp3);
        etOtp4 = view.findViewById(R.id.etOtp4);
        tvTimer = view.findViewById(R.id.tvTimer);
        tvResend = view.findViewById(R.id.tvResend);
        btnVerify = view.findViewById(R.id.btnVerify);

        // Find the email display TextView
        TextView tvEmailDisplay = view.findViewById(R.id.tvEmailDisplay);

        // Retrieve the email from the Bundle and display it
        if (getArguments() != null) {
            userEmail = getArguments().getString("USER_EMAIL", "your email address");
        }
        tvEmailDisplay.setText(userEmail);

        apiService = RetrofitClient.getClient(requireContext()).create(AuthApiService.class);

        setupOtpInputs();
        startTimer();

        // Handle Resend Click
        tvResend.setOnClickListener(v -> {
            Toast.makeText(getContext(), "A new code has been sent.", Toast.LENGTH_SHORT).show();
            startTimer();
        });

        // Handle Verify Click
        btnVerify.setOnClickListener(v -> attemptVerification(view));
        tvResend.setOnClickListener(v -> attemptResendOtp());

        view.findViewById(R.id.btnBack).setOnClickListener(v -> {
            Navigation.findNavController(view).popBackStack();
        });
    }

    private void attemptResendOtp() {
        if (userEmail.isEmpty()) return;

        // Disable button visually during network request
        tvResend.setEnabled(false);
        tvResend.setText("Sending...");

        apiService.resendOtp(userEmail).enqueue(new Callback<AuthResponse>() {
            @Override
            public void onResponse(Call<AuthResponse> call, Response<AuthResponse> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(getContext(), "New OTP sent!", Toast.LENGTH_SHORT).show();
                    startTimer(); // Restart the 130-second cooldown
                } else {
                    tvResend.setEnabled(true);
                    tvResend.setText("Resend OTP");
                    Toast.makeText(getContext(), "Failed to resend. Please try again.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<AuthResponse> call, Throwable t) {
                tvResend.setEnabled(true);
                tvResend.setText("Resend OTP");
                Toast.makeText(getContext(), "Network Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void attemptVerification(View view) {


        String otp = ((etOtp1.getText() != null) ? etOtp1.getText().toString().trim() : "")
                + ((etOtp2.getText() != null) ? etOtp2.getText().toString().trim() : "")
                + ((etOtp3.getText() != null) ? etOtp3.getText().toString().trim() : "")
                + ((etOtp4.getText() != null) ? etOtp4.getText().toString().trim() : "");

        if (otp.isEmpty()) {
            Toast.makeText(getContext(), "Please enter the OTP", Toast.LENGTH_SHORT).show();
            return;
        }

        if(otp.length() < 4){
            Toast.makeText(getContext(), "Please enter all 4 digits", Toast.LENGTH_SHORT).show();
            return;
        }

        if (userEmail.isEmpty()) {
            Toast.makeText(getContext(), "Email missing. Please sign up again.", Toast.LENGTH_SHORT).show();
            return;
        }

        btnVerify.setEnabled(false);
        btnVerify.setText("Verifying...");

        apiService.verifyOtp(userEmail, otp).enqueue(new Callback<AuthResponse>() {
            @Override
            public void onResponse(Call<AuthResponse> call, Response<AuthResponse> response) {
                btnVerify.setEnabled(true);
                btnVerify.setText("Verify Account");

                if (response.isSuccessful() && response.body() != null) {
                    AuthResponse authData = response.body();

                    // Securely save the session tokens
                    TokenManager tokenManager = new TokenManager(requireContext());
                    tokenManager.saveTokens(
                            authData.getToken(),
                            authData.getRefreshToken(),
                            authData.getUserId()
                    );

                    Toast.makeText(getContext(), "Email Verified Successfully!", Toast.LENGTH_SHORT).show();

                    NavOptions navOptions = new NavOptions.Builder()
                            // Change R.id.verifyOtpFragment to our nav graph's root start destination (e.g., loginFragment)
                            // This acts as a nuke, clearing EVERYTHING in the backstack up to and including the login/signup screens.
                            .setPopUpTo(R.id.loginFragment, true)
                            .build();

                    Navigation.findNavController(requireView()).navigate(
                            R.id.action_verifyOtpFragment_to_dashboardFragment,
                            null,
                            navOptions
                    );
                } else {
                    if (response.code() == 400) {
                        Toast.makeText(getContext(), "Invalid or Expired OTP", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getContext(), "Verification Failed", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<AuthResponse> call, Throwable t) {
                btnVerify.setEnabled(true);
                btnVerify.setText("Verify Account");
                Toast.makeText(getContext(), "Network Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupOtpInputs() {
        // Auto-advance to the next box when a digit is entered
        setNextFocusListener(etOtp1, etOtp2);
        setNextFocusListener(etOtp2, etOtp3);
        setNextFocusListener(etOtp3, etOtp4);

        // Auto-reverse to the previous box when backspace is pressed on an empty box
        setBackspaceListener(etOtp2, etOtp1);
        setBackspaceListener(etOtp3, etOtp2);
        setBackspaceListener(etOtp4, etOtp3);
    }

    private void setNextFocusListener(EditText current, EditText next) {
        current.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() == 1) {
                    next.requestFocus();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void setBackspaceListener(EditText current, EditText previous) {
        current.setOnKeyListener((v, keyCode, event) -> {
            if (keyCode == KeyEvent.KEYCODE_DEL && event.getAction() == KeyEvent.ACTION_DOWN) {
                if (current.getText().toString().isEmpty()) {
                    previous.requestFocus();
                    previous.setText("");
                    return true;
                }
            }
            return false;
        });
    }

    private void startTimer() {
        // Hide resend button, show timer text
        tvResend.setVisibility(View.GONE);
        tvTimer.setVisibility(View.VISIBLE);

        countDownTimer = new CountDownTimer(START_TIME_IN_MILLIS, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                int minutes = (int) (millisUntilFinished / 1000) / 60;
                int seconds = (int) (millisUntilFinished / 1000) % 60;

                String timeLeftFormatted = String.format(Locale.getDefault(), "Resend code in %02d:%02d", minutes, seconds);
                tvTimer.setText(timeLeftFormatted);
            }

            @Override
            public void onFinish() {
                // Time is up: Hide timer, show active Resend button
                tvTimer.setVisibility(View.GONE);
                tvResend.setVisibility(View.VISIBLE);
            }
        }.start();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Prevent memory leaks if the user leaves the page before the timer finishes
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
    }
}