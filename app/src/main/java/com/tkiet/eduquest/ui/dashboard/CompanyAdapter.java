package com.tkiet.eduquest.ui.dashboard;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.tkiet.eduquest.R;

import java.util.List;

public class CompanyAdapter extends RecyclerView.Adapter<CompanyAdapter.ViewHolder> {

    private final List<String> companyList;
    private final Context context;

    public CompanyAdapter(List<String> companyList, Context context) {
        this.companyList = companyList;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_company, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String companyName = companyList.get(position);
        holder.companyNameTextView.setText(companyName);

        // Set click listener to open details
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, CompanyDetailsActivity.class);
            intent.putExtra("companyName", companyName);
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return companyList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView companyNameTextView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            companyNameTextView = itemView.findViewById(R.id.company_name_text_view);
        }
    }
}
