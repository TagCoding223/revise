package com.revise;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.material.textfield.TextInputEditText;

public class SignupFragment extends Fragment {

    public SignupFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_signup, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        TextView tvLoginLink = view.findViewById(R.id.tvLoginLink);
        // Find the email input field
        TextInputEditText etEmail = view.findViewById(R.id.etEmail);

        // Navigate back to LoginFragment when clicked
        tvLoginLink.setOnClickListener(v -> {
            Navigation.findNavController(view).navigate(R.id.action_signupFragment_to_loginFragment);
        });

        // Navigate to Verify OTP and pass the email
        view.findViewById(R.id.btnSignup).setOnClickListener(v -> {
            // 1. Get the email from the input field
            String userEmail = etEmail.getText() != null ? etEmail.getText().toString().trim() : "";

            // 2. Create a Bundle to hold the data
            Bundle bundle = new Bundle();
            bundle.putString("USER_EMAIL", userEmail);

            // 3. Pass the bundle into the navigate action
            Navigation.findNavController(view).navigate(R.id.action_signupFragment_to_verifyOtpFragment, bundle);
        });
    }
}