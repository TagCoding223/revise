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
import androidx.navigation.Navigation;

import com.revise.R;

import java.util.Locale;

public class VerifyOtpFragment extends Fragment {

    private EditText etOtp1, etOtp2, etOtp3, etOtp4;
    private TextView tvTimer, tvResend;
    private CountDownTimer countDownTimer;

    // 130 seconds = 2 minutes and 10 seconds
    private static final long START_TIME_IN_MILLIS = 130000;

    public VerifyOtpFragment() {
        // Required empty public constructor
    }

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

        // NEW: Find the email display TextView
        TextView tvEmailDisplay = view.findViewById(R.id.tvEmailDisplay);

        // NEW: Retrieve the email from the Bundle and display it
        String passedEmail = "";
        if (getArguments() != null) {
            passedEmail = getArguments().getString("USER_EMAIL", "your email address");
        }
        tvEmailDisplay.setText(passedEmail);

        setupOtpInputs();
        startTimer();

        // Handle Resend Click
        tvResend.setOnClickListener(v -> {
            Toast.makeText(getContext(), "A new code has been sent.", Toast.LENGTH_SHORT).show();
            startTimer();
        });

        // Handle Verify Click
        view.findViewById(R.id.btnVerify).setOnClickListener(v -> {
            String otpCode = etOtp1.getText().toString() + etOtp2.getText().toString() +
                    etOtp3.getText().toString() + etOtp4.getText().toString();

            if (otpCode.length() < 4) {
                Toast.makeText(getContext(), "Please enter all 4 digits", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getContext(), "Verifying: " + otpCode, Toast.LENGTH_SHORT).show();

                // TODO: Add actual Retrofit API call here later.
                // For now, simulate a successful verification and navigate to Dashboard:
                Navigation.findNavController(view).navigate(R.id.action_verifyOtpFragment_to_dashboardFragment);
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