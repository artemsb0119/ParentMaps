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

public class SearchAdapter extends RecyclerView.Adapter<SearchAdapter.SearchViewHolder>{

    private List<User> users = new ArrayList<>();

    private SearchAdapter.OnUserClickListener onUserClickListener;

    public void setOnUserClickListener(SearchAdapter.OnUserClickListener onUserClickListener) {
        this.onUserClickListener = onUserClickListener;
    }

    public void setUsers(List<User> users) {
        this.users = users;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public SearchAdapter.SearchViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.user_item_add, parent, false);
        return new SearchAdapter.SearchViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SearchAdapter.SearchViewHolder holder, int position) {
        User user = users.get(position);
        String userInfo = String.format("%s %s", user.getName(), user.getLastName());
        holder.textViewUserInfo.setText(userInfo);
        if (user.getChildOrParent().equals("Ребенок")){
            holder.imageViewChildParent.setImageResource(R.drawable.child);
        } else {
            holder.imageViewChildParent.setImageResource(R.drawable.parent);
        }
        holder.imageViewAdd.setImageResource(R.drawable.plus);
        holder.imageViewAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                holder.imageViewAdd.setImageResource(R.drawable.galka);
                if (onUserClickListener!=null) {
                    onUserClickListener.onUserClick(user);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    interface OnUserClickListener {

        void onUserClick(User user);
    }

    static class SearchViewHolder extends RecyclerView.ViewHolder {

        private TextView textViewUserInfo;
        private ImageView imageViewChildParent;
        private ImageView imageViewAdd;

        public SearchViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewUserInfo = itemView.findViewById(R.id.textViewUserInfo);
            imageViewChildParent = itemView.findViewById(R.id.imageViewChildParent);
            imageViewAdd = itemView.findViewById(R.id.imageViewAdd);
        }
    }
}
