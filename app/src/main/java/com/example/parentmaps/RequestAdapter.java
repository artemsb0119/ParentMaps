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

public class RequestAdapter extends RecyclerView.Adapter<RequestAdapter.RequestViewHolder>{

    private List<User> users = new ArrayList<>();

    private RequestAdapter.OnPlusClickListener onPlusClickListener;
    private RequestAdapter.OnMinusClickListener onMinusClickListener;

    public void setOnPlusClickListener(RequestAdapter.OnPlusClickListener onPlusClickListener) {
        this.onPlusClickListener = onPlusClickListener;
    }

    public void setOnMinusClickListener(RequestAdapter.OnMinusClickListener onMinusClickListener) {
        this.onMinusClickListener = onMinusClickListener;
    }

    public void setUsers(List<User> users) {
        this.users = users;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public RequestAdapter.RequestViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.user_item_request, parent, false);
        return new RequestAdapter.RequestViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RequestAdapter.RequestViewHolder holder, int position) {
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
                if (onPlusClickListener!=null) {
                    onPlusClickListener.onPlusClick(user);
                }
            }
        });
        holder.imageViewMinus.setImageResource(R.drawable.minus);
        holder.imageViewMinus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (onMinusClickListener!=null) {
                    onMinusClickListener.onMinusClick(user);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    interface OnPlusClickListener {

        void onPlusClick(User user);
    }

    interface OnMinusClickListener {

        void onMinusClick(User user);
    }

    static class RequestViewHolder extends RecyclerView.ViewHolder {

        private TextView textViewUserInfo;
        private ImageView imageViewChildParent;
        private ImageView imageViewAdd;
        private ImageView imageViewMinus;

        public RequestViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewUserInfo = itemView.findViewById(R.id.textViewUserInfo);
            imageViewChildParent = itemView.findViewById(R.id.imageViewChildParent);
            imageViewAdd = itemView.findViewById(R.id.imageViewAdd);
            imageViewMinus = itemView.findViewById(R.id.imageViewMinus);
        }
    }
}

