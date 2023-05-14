package com.example.parentmaps;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class FavouriteAdapter extends RecyclerView.Adapter<FavouriteAdapter.FavouriteViewHolder> {

    private List<String> addresses = new ArrayList<>();

    public void setAddresses(List<String> adresses) {
        this.addresses = adresses;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public FavouriteAdapter.FavouriteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.history_item, parent, false);
        return new FavouriteAdapter.FavouriteViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FavouriteAdapter.FavouriteViewHolder holder, int position) {
        String adres = addresses.get(position);
        holder.textViewHistory.setText(adres);
    }

    @Override
    public int getItemCount() {
        return addresses.size();
    }

    static class FavouriteViewHolder extends RecyclerView.ViewHolder {

        private TextView textViewHistory;

        public FavouriteViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewHistory = itemView.findViewById(R.id.textViewHistory);
        }
    }
}
