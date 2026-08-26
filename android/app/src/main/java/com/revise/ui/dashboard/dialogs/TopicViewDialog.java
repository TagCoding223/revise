package com.revise.ui.dashboard.dialogs;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.revise.R;
import com.revise.model.Topic;

public class TopicViewDialog extends DialogFragment {

    private final Topic topic;

    public TopicViewDialog(Topic topic) {
        this.topic = topic;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View view = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_view_topic, null);

        ((TextView) view.findViewById(R.id.tvViewTitle)).setText(topic.getTitle());
        ((TextView) view.findViewById(R.id.tvViewStage)).setText("Stage " + topic.getStage());

        TextView tvDesc = view.findViewById(R.id.tvViewDesc);
        if (topic.getDescription() == null || topic.getDescription().trim().isEmpty()) {
            tvDesc.setText("No description provided.");
            tvDesc.setTextColor(Color.GRAY);
        } else {
            tvDesc.setText(topic.getDescription());
        }

        AlertDialog dialog = new MaterialAlertDialogBuilder(requireContext())
                .setView(view)
                .setBackground(new ColorDrawable(Color.TRANSPARENT))
                .create();

        view.findViewById(R.id.btnCloseView).setOnClickListener(v -> dialog.dismiss());

        return dialog;
    }
}