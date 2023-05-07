package com.example.parentmaps;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import java.util.List;

public class RequestActivity extends AppCompatActivity {

    private RecyclerView recyclerViewRequest;
    private RequestAdapter requestAdapter;
    private RequestViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_request);
        initViews();
        viewModel = new ViewModelProvider(this).get(RequestViewModel.class);
        observeViewModel();
        requestAdapter.setOnPlusClickListener(new RequestAdapter.OnPlusClickListener() {
            @Override
            public void onPlusClick(User user) {
                viewModel.addUser(user);
            }
        });
        requestAdapter.setOnMinusClickListener(new RequestAdapter.OnMinusClickListener() {
            @Override
            public void onMinusClick(User user) {
                viewModel.deleteUser(user);
            }
        });
    }

    private void initViews() {
        recyclerViewRequest = findViewById(R.id.recyclerViewRequest);
        requestAdapter = new RequestAdapter();
        recyclerViewRequest.setAdapter(requestAdapter);
    }

    private void observeViewModel() {
        viewModel.getUsers().observe(this, new Observer<List<User>>() {
            @Override
            public void onChanged(List<User> users) {
                requestAdapter.setUsers(users);
            }
        });
    }

    public static Intent newIntent(Context context) {
        return new Intent(context, RequestActivity.class);
    }
}