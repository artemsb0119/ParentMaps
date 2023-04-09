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

public class UsersAdapter extends RecyclerView.Adapter<UsersAdapter.UserViewHolder>{

    private List<User> users = new ArrayList<>();

    private OnUserClickListener onUserClickListener;

    public void setOnUserClickListener(OnUserClickListener onUserClickListener) {
        this.onUserClickListener = onUserClickListener;
    }

    public void setUsers(List<User> users) {
        this.users = users;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.user_item, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        User user = users.get(position);
        String userInfo = String.format("%s %s", user.getName(), user.getLastName());
        holder.textViewUserInfo.setText(userInfo);
        if (user.getChildOrParent().equals("Ребенок")){
            holder.imageViewChildParent.setImageResource(R.drawable.child);
        } else {
            holder.imageViewChildParent.setImageResource(R.drawable.parent);
        }
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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

    static class UserViewHolder extends RecyclerView.ViewHolder {

        private TextView textViewUserInfo;
        private ImageView imageViewChildParent;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewUserInfo = itemView.findViewById(R.id.textViewUserInfo);
            imageViewChildParent = itemView.findViewById(R.id.imageViewChildParent);
        }
    }
}
