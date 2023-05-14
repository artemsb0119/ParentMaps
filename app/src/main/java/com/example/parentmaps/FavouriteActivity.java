package com.example.parentmaps;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import java.util.List;

public class FavouriteActivity extends AppCompatActivity {

    private Button buttonAdd;
    private FavouriteAdapter favouriteAdapter;
    private FavouriteViewModel viewModel;
    private RecyclerView recyclerViewLocate;

    private String childId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favourite);
        childId = getIntent().getStringExtra("user");
        initViews();
        viewModel = new ViewModelProvider(this, new FavouriteViewModelFactory(this.getApplication(),childId))
                .get(FavouriteViewModel.class);
        buttonAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = AddLocationActivity.newIntent(FavouriteActivity.this);
                intent.putExtra("user", childId);
                startActivity(intent);
            }
        });
        observeViewModel();
    }
    private void initViews() {
        recyclerViewLocate = findViewById(R.id.recyclerViewLocate);
        favouriteAdapter = new FavouriteAdapter();
        recyclerViewLocate.setAdapter(favouriteAdapter);
        buttonAdd = findViewById(R.id.buttonAdd);
    }
    private void observeViewModel() {
        viewModel.getAddresses().observe(this, new Observer<List<String>>() {
            @Override
            public void onChanged(List<String> strings) {
                favouriteAdapter.setAddresses(strings);
            }
        });
    }
    public static Intent newIntent(Context context) {
        return new Intent(context, FavouriteActivity.class);
    }
}