package com.example.parentmaps;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;

public class SearchActivity extends AppCompatActivity {

    private RecyclerView recyclerViewSearch;
    private SearchAdapter searchAdapter;
    private SearchViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        initViews();
        viewModel = new ViewModelProvider(this).get(SearchViewModel.class);
        observeViewModel();
        searchAdapter.setOnUserClickListener(new SearchAdapter.OnUserClickListener() {
            @Override
            public void onUserClick(User user) {
                viewModel.request(user);
            }
        });

    }

    private void initViews() {
        recyclerViewSearch = findViewById(R.id.recyclerViewSearch);
        searchAdapter = new SearchAdapter();
        recyclerViewSearch.setAdapter(searchAdapter);
    }

    private void observeViewModel() {
        viewModel.getUsers().observe(this, new Observer<List<User>>() {
            @Override
            public void onChanged(List<User> users) {
                searchAdapter.setUsers(users);
            }
        });
    }

    public static Intent newIntent(Context context) {
        return new Intent(context, SearchActivity.class);
    }
}