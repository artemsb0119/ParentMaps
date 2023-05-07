package com.example.parentmaps;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;

import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.yandex.mapkit.Animation;
import com.yandex.mapkit.geometry.Point;
import com.yandex.mapkit.map.CameraPosition;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class LocationService extends Service {
    private SharedPreferences sharedPreferences;
    private LocationManager locationManager;
    private Point currentLocation;
    private FirebaseAuth auth;
    private String lastAddress = "";

    private Handler handler;
    private Runnable runnable;

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
        startForeground(1, createNotification());
        sharedPreferences =getSharedPreferences("my_preferences", MODE_PRIVATE);
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        criteria.setPowerRequirement(Criteria.POWER_HIGH);
        criteria.setAltitudeRequired(true);
        criteria.setBearingRequired(true);
        criteria.setSpeedRequired(true);
        String provider = locationManager.getBestProvider(criteria, true);

        LocationListener locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                if (location != null && (location.getAccuracy() < 100)) {
                    currentLocation = new Point(location.getLatitude(), location.getLongitude());
                }
            }

            @Override
            public void onProviderEnabled(String provider) {
                Log.d("LocationService", "onProviderEnabled: " + provider);
            }

            @Override
            public void onProviderDisabled(String provider) {
                Log.d("LocationService", "onProviderDisabled: " + provider);
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {
                Log.d("LocationService", "onStatusChanged: " + provider);
            }
        };

        if (provider != null) {
            locationManager.requestLocationUpdates(provider, 1000, 10, locationListener);
            Location location = locationManager.getLastKnownLocation(provider);
            if (location != null) {
                currentLocation = new Point(location.getLatitude(), location.getLongitude());
            }
        }
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
                    "channel_id",
                    "channel_name",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            notificationManager.createNotificationChannel(notificationChannel);
        }
    }

    public Notification createNotification() {
        Notification builder = new NotificationCompat.Builder(this, "channel_id")
                .setSmallIcon(R.drawable.icon_map)
                .setContentTitle("Приложение отслеживает текущее местоположение")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .build();
        return builder;
    }

    /*private void startGettingAddress() {
        handler = new Handler();
        runnable = new Runnable() {
            @Override
            public void run() {
                Log.d("iii","do");
                if (currentLocation != null) {
                    getAddressFromLocation(currentLocation.getLatitude(), currentLocation.getLongitude());
                }
                handler.postDelayed(this, 60000);
            }
        };
        handler.postDelayed(runnable, 60000);
    }


    private void getAddressFromLocation(double latitude, double longitude) {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        auth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = auth.getCurrentUser();
        String currentUserId = currentUser.getUid();
        try {
            List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
            if (addresses != null && !addresses.isEmpty()) {
                Address address = addresses.get(0);
                if (lastAddress.equals("")||!lastAddress.equals(address.getAddressLine(0))) {
                    lastAddress = address.getAddressLine(0);
                    DateFormat dateFormat = new SimpleDateFormat("dd.MM HH:mm", Locale.getDefault());
                    String currentTime = dateFormat.format(new Date());
                    String addressString = currentTime + " " + address.getAddressLine(0);
                    DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("ChildLocation").child(currentUserId);
                    databaseReference.push().setValue(addressString);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }*/
}
