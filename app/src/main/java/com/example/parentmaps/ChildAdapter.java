package com.example.parentmaps;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class ChildAdapter extends RecyclerView.Adapter<ChildAdapter.ChildViewHolder> {

    private List<String> addresses = new ArrayList<>();

    public void setAddresses(List<String> adresses) {
        this.addresses = adresses;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ChildViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.history_item, parent, false);
        return new ChildViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChildViewHolder holder, int position) {
        String adres = addresses.get(position);
        holder.textViewHistory.setText(adres);
    }

    @Override
    public int getItemCount() {
        return addresses.size();
    }

    static class ChildViewHolder extends RecyclerView.ViewHolder {

        private TextView textViewHistory;

        public ChildViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewHistory = itemView.findViewById(R.id.textViewHistory);
        }
    }
}
