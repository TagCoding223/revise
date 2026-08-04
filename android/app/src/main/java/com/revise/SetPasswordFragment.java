package com.revise; // Ensure this matches your package

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;

public class SetPasswordFragment extends Fragment {

    public SetPasswordFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_set_password, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ImageButton btnBack = view.findViewById(R.id.btnBack);
        TextInputEditText etNewPassword = view.findViewById(R.id.etNewPassword);
        TextInputEditText etConfirmPassword = view.findViewById(R.id.etConfirmPassword);

        // 1. Handle Back Button (Returns to Profile)
        btnBack.setOnClickListener(v -> {
            Navigation.findNavController(view).popBackStack();
        });

        // 2. Handle Save Password Button
        view.findViewById(R.id.btnSavePassword).setOnClickListener(v -> {
            String newPassword = etNewPassword.getText() != null ? etNewPassword.getText().toString() : "";
            String confirmPassword = etConfirmPassword.getText() != null ? etConfirmPassword.getText().toString() : "";

            if (newPassword.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(getContext(), "Please fill out all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!newPassword.equals(confirmPassword)) {
                Toast.makeText(getContext(), "Passwords do not match", Toast.LENGTH_SHORT).show();
                return;
            }

            if (newPassword.length() < 8) {
                Toast.makeText(getContext(), "Password must be at least 8 characters", Toast.LENGTH_SHORT).show();
                return;
            }

            // TODO: Add Retrofit API call to POST /api/v1/users/set-password
            Toast.makeText(getContext(), "Password changed successfully!", Toast.LENGTH_SHORT).show();

            // Navigate back to the previous screen on success
            Navigation.findNavController(view).popBackStack();
        });
    }
}