package com.tkiet.eduquest.ui.account;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.tkiet.eduquest.R;

import java.util.List;

public class CertificationsAdapter extends RecyclerView.Adapter<CertificationsAdapter.ViewHolder> {

    private List<String> certificationsList;
    private OnCertificationRemoveListener removeListener;

    public interface OnCertificationRemoveListener {
        void onRemove(String certification);
    }

    public CertificationsAdapter(List<String> certificationsList, OnCertificationRemoveListener removeListener) {
        this.certificationsList = certificationsList;
        this.removeListener = removeListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_certification, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String certification = certificationsList.get(position);
        holder.certificationTextView.setText(certification);

        holder.removeButton.setOnClickListener(v -> {
            if (removeListener != null) {
                removeListener.onRemove(certification);
            }
        });
    }

    @Override
    public int getItemCount() {
        return certificationsList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView certificationTextView;
        ImageButton removeButton;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            certificationTextView = itemView.findViewById(R.id.certification_text);
            removeButton = itemView.findViewById(R.id.remove_button);
        }
    }
}
