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

    private final List<Topic> topicList;

    public TopicAdapter(List<Topic> topicList) {
        this.topicList = topicList;
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
        }

        // Mock Click Listeners
        holder.btnRevise.setOnClickListener(v ->
                Toast.makeText(v.getContext(), "Revise clicked for " + topic.getTitle(), Toast.LENGTH_SHORT).show()
        );
        holder.btnView.setOnClickListener(v ->
                Toast.makeText(v.getContext(), "View clicked", Toast.LENGTH_SHORT).show()
        );
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