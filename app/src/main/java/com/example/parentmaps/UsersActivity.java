package com.example.parentmaps;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.yandex.mapkit.MapKitFactory;

import java.util.List;

public class UsersActivity extends AppCompatActivity {

    private RecyclerView recyclerViewUsers;
    private UsersAdapter usersAdapter;
    private UsersViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Intent usersServiceIntent = new Intent(this, UsersService.class);
        stopService(usersServiceIntent);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_users);
        initViews();
        viewModel = new ViewModelProvider(this).get(UsersViewModel.class);
        observeViewModel();
        usersAdapter.setOnUserClickListener(new UsersAdapter.OnUserClickListener() {
            @Override
            public void onUserClick(User user) {
                Intent intent = ChildActivity.newIntent(UsersActivity.this);
                intent.putExtra("user", user.getId());
                startActivity(intent);
            }
        });
    }

    private void initViews() {
        recyclerViewUsers = findViewById(R.id.recyclerViewUsers);
        usersAdapter = new UsersAdapter();
        recyclerViewUsers.setAdapter(usersAdapter);
    }

    private void observeViewModel() {
        viewModel.getUser().observe(this, new Observer<FirebaseUser>() {
            @Override
            public void onChanged(FirebaseUser firebaseUser) {
                if (firebaseUser == null) {
                    Intent intent = LoginActivity.newIntent(UsersActivity.this);
                    startActivity(intent);
                    finish();
                }
            }
        });
        viewModel.getUsers().observe(this, new Observer<List<User>>() {
            @Override
            public void onChanged(List<User> users) {
                usersAdapter.setUsers(users);
            }
        });
        viewModel.getNotificate().observe(this, new Observer<String>() {
            private String previousValue; // Предыдущее значение

            @Override
            public void onChanged(String s) {
                if (s != null) {
                    if (previousValue == null) {
                        previousValue = s;
                    } else if (!s.equals(previousValue)) {
                        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), "channel_id")
                                .setSmallIcon(R.drawable.icon_map)
                                .setContentTitle("Parent Maps")
                                .setContentText(s)
                                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

                        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(getApplicationContext());
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            NotificationChannel channel = new NotificationChannel("channel_id", "Channel name", NotificationManager.IMPORTANCE_DEFAULT);
                            notificationManager.createNotificationChannel(channel);
                        }

                        notificationManager.notify(1, builder.build());

                        previousValue = s; // Обновляем предыдущее значение только в случае изменения
                    }
                }
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        Intent usersServiceIntent = new Intent(this, UsersService.class);
        stopService(usersServiceIntent);
    }

    @Override
    protected void onStop() {
        Intent usersServiceIntent = new Intent(this, UsersService.class);
        ContextCompat.startForegroundService(this, usersServiceIntent);
        super.onStop();
    }

    public static Intent newIntent(Context context) {
        return new Intent(context, UsersActivity.class);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.item_search) {
            Intent intent = SearchActivity.newIntent(UsersActivity.this);
            startActivity(intent);
        }
        if (item.getItemId() == R.id.item_Logout) {
            viewModel.logout();
        }
        return super.onOptionsItemSelected(item);
    }
}