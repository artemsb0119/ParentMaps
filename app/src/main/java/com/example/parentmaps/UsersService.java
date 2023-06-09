package com.example.parentmaps;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModelProvider;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;


public class UsersService extends Service {
    private FirebaseAuth auth;
    private FirebaseDatabase database;
    private DatabaseReference usersReference;
    private DatabaseReference friendReference;
    private DatabaseReference notificateReference;
    private MutableLiveData<String> notificate = new MutableLiveData<>();

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
        startForeground(2, createNotification());
        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        usersReference = database.getReference("Users");
        friendReference = database.getReference("Friends");
        notificateReference = database.getReference("SendNotificate");
        friendReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                FirebaseUser currentUser = auth.getCurrentUser();
                if (currentUser == null) {
                    return;
                }
                List<Friends> friend = new ArrayList<>();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Friends friends = dataSnapshot.getValue(Friends.class);
                    if (friends == null) {
                        continue;
                    }
                    if (friends.getUserId2().equals(currentUser.getUid())) {
                        friend.add(friends);
                    }
                }
                List<User> usersFromDb = new ArrayList<>();
                for (Friends friends : friend) {

                    notificateReference.child(friends.getUserId1()).addValueEventListener(new ValueEventListener() {
                        private String previousValue; // Предыдущее значение

                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            String newValue = snapshot.getValue(String.class);
                            if (newValue != null) {
                                if (previousValue == null) {
                                    previousValue = newValue;
                                } else if (!newValue.equals(previousValue)) {
                                    notificate.setValue(newValue);
                                    NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), "channel_id")
                                            .setSmallIcon(R.drawable.icon_map)
                                            .setContentTitle("Parent Maps")
                                            .setContentText(newValue)
                                            .setPriority(NotificationCompat.PRIORITY_DEFAULT);

                                    NotificationManagerCompat notificationManager = NotificationManagerCompat.from(getApplicationContext());
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                        NotificationChannel channel = new NotificationChannel("channel_id", "Channel name", NotificationManager.IMPORTANCE_DEFAULT);
                                        notificationManager.createNotificationChannel(channel);
                                    }

                                    notificationManager.notify(1, builder.build());

                                    previousValue = newValue; // Обновляем предыдущее значение только в случае изменения
                                }
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });


                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;

    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    public void createNotificationChannel() {
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = new NotificationChannel(
                    "channel_id1",
                    "channel_name1",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            notificationManager.createNotificationChannel(notificationChannel);
        }
    }

    public Notification createNotification() {
        Notification builder = new NotificationCompat.Builder(this, "channel_id1")
                .setSmallIcon(R.drawable.icon_map)
                .setContentTitle("Приложение работает")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .build();
        return builder;
    }
}
