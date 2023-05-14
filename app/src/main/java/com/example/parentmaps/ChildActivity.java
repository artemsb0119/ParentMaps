package com.example.parentmaps;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import java.util.List;

public class ChildActivity extends AppCompatActivity {

    private RecyclerView recyclerViewHistory;
    private ChildAdapter childAdapter;
    private ChildViewModel viewModel;
    private TextView textViewLocate;
    private String childId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_child);
        childId = getIntent().getStringExtra("user");
        initViews();
        viewModel = new ViewModelProvider(this, new ChildViewModelFactory(this.getApplication(),childId))
                .get(ChildViewModel.class);
        observeViewModel();
    }

    private void initViews() {
        recyclerViewHistory = findViewById(R.id.recyclerViewHistory);
        childAdapter = new ChildAdapter();
        recyclerViewHistory.setAdapter(childAdapter);
        textViewLocate = findViewById(R.id.textViewLocate);
    }

    private void observeViewModel() {
        viewModel.getAddresses().observe(this, new Observer<List<String>>() {
            @Override
            public void onChanged(List<String> strings) {
                childAdapter.setAddresses(strings);
            }
        });
        viewModel.getLocation().observe(this, new Observer<String>() {
            @Override
            public void onChanged(String s) {
                textViewLocate.setText(s);
            }
        });
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.child_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.item_history) {
            Intent intent = HistoryActivity.newIntent(ChildActivity.this);
            intent.putExtra("user", childId);
            startActivity(intent);
        } else if (item.getItemId() == R.id.item_location) {
            Intent intent = FavouriteActivity.newIntent(ChildActivity.this);
            intent.putExtra("user", childId);
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }

    public static Intent newIntent(Context context) {
        return new Intent(context, ChildActivity.class);
    }
}