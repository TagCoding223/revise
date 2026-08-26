package com.revise.ui.dashboard.dialogs;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.revise.R;
import com.revise.dto.request.TopicRequest;
import com.revise.model.Topic;
import java.util.ArrayList;
import java.util.List;

public class TopicFormDialog extends DialogFragment {

    private final Topic existingTopic;
    private final FormSubmitListener submitListener;

    public interface FormSubmitListener {
        void onSubmit(TopicRequest request, @Nullable Topic existingTopic);
    }

    public TopicFormDialog(@Nullable Topic existingTopic, FormSubmitListener submitListener) {
        this.existingTopic = existingTopic;
        this.submitListener = submitListener;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View view = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_topic_form, null);

        TextView tvTitle = view.findViewById(R.id.tvFormTitle);
        EditText etTopicTitle = view.findViewById(R.id.etTopicTitle);
        EditText etTopicDesc = view.findViewById(R.id.etTopicDesc);
        LinearLayout layoutLinks = view.findViewById(R.id.layoutLinksContainer);

        // Pre-fill data if updating
        if (existingTopic != null) {
            tvTitle.setText("Update Topic");
            etTopicTitle.setText(existingTopic.getTitle());
            etTopicDesc.setText(existingTopic.getDescription());

            if (existingTopic.getLinks() != null && !existingTopic.getLinks().isEmpty()) {
                for (String link : existingTopic.getLinks()) {
                    addDynamicLinkView(layoutLinks, link);
                }
            } else {
                addDynamicLinkView(layoutLinks, "");
            }
        } else {
            tvTitle.setText("Create Topic");
            addDynamicLinkView(layoutLinks, "");
        }

        // View Listeners
        view.findViewById(R.id.btnAddLink).setOnClickListener(v -> addDynamicLinkView(layoutLinks, ""));

        AlertDialog dialog = new MaterialAlertDialogBuilder(requireContext())
                .setView(view)
                .setBackground(new ColorDrawable(Color.TRANSPARENT))
                .create();

        view.findViewById(R.id.btnCancel).setOnClickListener(v -> dialog.dismiss());

        // Validate and Save
        view.findViewById(R.id.btnSave).setOnClickListener(v -> {
            String title = etTopicTitle.getText().toString().trim();
            if (title.isEmpty()) {
                Toast.makeText(getContext(), "Title is required", Toast.LENGTH_SHORT).show();
                return;
            }

            List<String> finalLinks = new ArrayList<>();
            for (int i = 0; i < layoutLinks.getChildCount(); i++) {
                View linkView = layoutLinks.getChildAt(i);
                EditText etLink = linkView.findViewById(R.id.etLinkUrl);
                String linkText = etLink.getText().toString().trim();

                if (!linkText.isEmpty()) {
                    if (!Patterns.WEB_URL.matcher(linkText).matches()) {
                        Toast.makeText(getContext(), "Invalid link: " + linkText, Toast.LENGTH_LONG).show();
                        return; // Stop execution
                    }
                    finalLinks.add(linkText);
                }
            }

            TopicRequest request = new TopicRequest(title, etTopicDesc.getText().toString().trim(), finalLinks);
            submitListener.onSubmit(request, existingTopic);
            dialog.dismiss();
        });

        return dialog;
    }

    private void addDynamicLinkView(LinearLayout container, String existingUrl) {
        View linkView = LayoutInflater.from(getContext()).inflate(R.layout.item_dynamic_link, container, false);
        ((EditText) linkView.findViewById(R.id.etLinkUrl)).setText(existingUrl);
        linkView.findViewById(R.id.btnRemoveLink).setOnClickListener(v -> container.removeView(linkView));
        container.addView(linkView);
    }
}