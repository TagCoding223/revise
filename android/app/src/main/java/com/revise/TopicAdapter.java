package com.revise;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import java.util.List;

public class TopicAdapter extends RecyclerView.Adapter<TopicAdapter.TopicViewHolder> {
    // Create an interface to handle clicks
    public interface OnTopicClickListener {
        void onViewClick(Topic topic);
        void onEditClick(Topic topic);
        void onDeleteClick(Topic topic);
        void onReviseClick(Topic topic);
    }
    private final List<Topic> topicList;
    private final OnTopicClickListener listener; //  Add the listener variable

    // Update constructor to require the listener
    public TopicAdapter(List<Topic> topicList, OnTopicClickListener listener) {
        this.topicList = topicList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public TopicViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_topic_card, parent, false);
        return new TopicViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TopicViewHolder holder, int position) {
        Topic topic = topicList.get(position);

        holder.tvTopicTitle.setText(topic.getTitle());
        holder.tvTopicDesc.setText(topic.getDescription());
        holder.tvStageBadge.setText("Stage " + topic.getStage());

        // Apply active/disabled UI logic
        if (!topic.getCategory().equals("today")) {
            // Fade the card and disable the revise button for tomorrow/upcoming
            holder.cardContainer.setAlpha(0.6f);
            holder.btnRevise.setEnabled(false);
            holder.btnRevise.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#E2E8F0"))); // Light gray
            holder.btnRevise.setTextColor(Color.parseColor("#94A3B8"));
        } else {
            // Active state
            holder.cardContainer.setAlpha(1.0f);
            holder.btnRevise.setEnabled(true);
            holder.btnRevise.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#2563EB"))); // Primary blue
            holder.btnRevise.setTextColor(Color.WHITE);
        }

        // 4. Wire the buttons to the listener interface instead of Toasts
        holder.btnView.setOnClickListener(v -> listener.onViewClick(topic));
        holder.btnEdit.setOnClickListener(v -> listener.onEditClick(topic));
        holder.btnDelete.setOnClickListener(v -> listener.onDeleteClick(topic));
        holder.btnRevise.setOnClickListener(v -> listener.onReviseClick(topic));
    }

    @Override
    public int getItemCount() {
        return topicList.size();
    }

    static class TopicViewHolder extends RecyclerView.ViewHolder {
        MaterialCardView cardContainer;
        TextView tvTopicTitle, tvTopicDesc, tvStageBadge;
        MaterialButton btnRevise;
        View btnView, btnEdit, btnDelete;

        public TopicViewHolder(@NonNull View itemView) {
            super(itemView);
            cardContainer = itemView.findViewById(R.id.cardContainer);
            tvTopicTitle = itemView.findViewById(R.id.tvTopicTitle);
            tvTopicDesc = itemView.findViewById(R.id.tvTopicDesc);
            tvStageBadge = itemView.findViewById(R.id.tvStageBadge);
            btnRevise = itemView.findViewById(R.id.btnRevise);
            btnView = itemView.findViewById(R.id.btnView);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}