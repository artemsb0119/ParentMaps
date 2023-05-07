package com.example.parentmaps;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.yandex.mapkit.MapKit;
import com.yandex.mapkit.MapKitFactory;
import com.yandex.mapkit.mapview.MapView;

public class HistoryActivity extends AppCompatActivity {

    private final String MAPKIT_API_KEY = "4659ffc0-0584-4295-9bb3-a57a4321726e";

    private MapView mapView;
    private MapKit mapKit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        MapKitFactory.setApiKey(MAPKIT_API_KEY);
        MapKitFactory.initialize(this);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        mapView = findViewById(R.id.mapview);
        mapKit = MapKitFactory.getInstance();
    }

    @Override
    protected void onStart() {
        MapKitFactory.getInstance().onStart();
        mapView.onStart();
        super.onStart();
    }

    @Override
    protected void onStop() {
        mapView.onStop();
        MapKitFactory.getInstance().onStop();
        super.onStop();
    }

    public static Intent newIntent(Context context) {
        return new Intent(context, HistoryActivity.class);
    }
}