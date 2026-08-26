package com.revise.ui.dashboard.dialogs;

import android.content.Context;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public class ActionDialogHelper {

    public static void showDeleteConfirmation(Context context, String topicTitle, Runnable onConfirm) {
        new MaterialAlertDialogBuilder(context)
                .setTitle("Delete Revision Topic")
                .setMessage("Are you sure you want to delete '" + topicTitle + "'? This action cannot be undone.")
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .setPositiveButton("Delete", (dialog, which) -> {
                    onConfirm.run();
                    dialog.dismiss();
                })
                .show();
    }

    public static void showLogoutConfirmation(Context context, Runnable onConfirm) {
        new MaterialAlertDialogBuilder(context)
                .setTitle("Log Out")
                .setMessage("Are you sure you want to log out of your account?")
                .setPositiveButton("Log Out", (dialog, which) -> {
                    onConfirm.run();
                    dialog.dismiss();
                })
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .show();
    }
}