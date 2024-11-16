package com.tkiet.eduquest.ui.account;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.tkiet.eduquest.R;

import java.util.List;

public class SkillsAdapter extends RecyclerView.Adapter<SkillsAdapter.SkillViewHolder> {

    private final List<String> skills;
    private final OnSkillRemoveListener removeListener;

    public interface OnSkillRemoveListener {
        void onSkillRemove(String skill);
    }

    public SkillsAdapter(List<String> skills, OnSkillRemoveListener removeListener) {
        this.skills = skills;
        this.removeListener = removeListener;
    }

    @NonNull
    @Override
    public SkillViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_skill, parent, false);
        return new SkillViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SkillViewHolder holder, int position) {
        String skill = skills.get(position);
        holder.skillName.setText(skill);
        holder.removeSkillButton.setOnClickListener(v -> removeListener.onSkillRemove(skill));
    }

    @Override
    public int getItemCount() {
        return skills.size();
    }

    static class SkillViewHolder extends RecyclerView.ViewHolder {
        TextView skillName;
            ImageButton removeSkillButton;

        SkillViewHolder(@NonNull View itemView) {
            super(itemView);
            skillName = itemView.findViewById(R.id.skill_name);
            removeSkillButton = itemView.findViewById(R.id.remove_skill_button);
        }
    }
}
