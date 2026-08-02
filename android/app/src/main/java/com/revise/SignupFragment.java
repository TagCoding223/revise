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

        // Navigate back to LoginFragment when clicked
        tvLoginLink.setOnClickListener(v -> {
            // Using the action we defined in nav_graph.xml to ensure the back stack clears properly
            Navigation.findNavController(view).navigate(R.id.action_signupFragment_to_loginFragment);
        });

        // Navigate to Verify OTP when Signup button is clicked
        view.findViewById(R.id.btnSignup).setOnClickListener(v -> {
            Navigation.findNavController(view).navigate(R.id.action_signupFragment_to_verifyOtpFragment);
        });
    }
}