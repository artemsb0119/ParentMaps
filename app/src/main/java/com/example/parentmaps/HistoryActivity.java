package com.example.parentmaps;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.yandex.mapkit.Animation;
import com.yandex.mapkit.MapKit;
import com.yandex.mapkit.MapKitFactory;
import com.yandex.mapkit.geometry.Point;
import com.yandex.mapkit.map.CameraPosition;
import com.yandex.mapkit.map.MapObjectCollection;
import com.yandex.mapkit.mapview.MapView;

import java.util.ArrayList;
import java.util.List;

public class HistoryActivity extends AppCompatActivity {

    private final String MAPKIT_API_KEY = "4659ffc0-0584-4295-9bb3-a57a4321726e";

    private MapView mapView;
    private MapObjectCollection mapObjectCollection;
    private MapKit mapKit;

    private FirebaseAuth auth;
    private String childId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        MapKitFactory.setApiKey(MAPKIT_API_KEY);
        MapKitFactory.initialize(this);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);
        childId = getIntent().getStringExtra("user");


        mapKit = MapKitFactory.getInstance();
        mapObjectCollection = mapView.getMap().getMapObjects().addCollection();

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference childLocationRef = database.getReference("ChildLocation");
        childLocationRef.child(childId).child("Locate").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                List<Point> points = new ArrayList<>();
                for (DataSnapshot coordinateSnapshot : dataSnapshot.getChildren()) {
                    Double latitude = coordinateSnapshot.child("latitude").getValue(Double.class);
                    Double longitude = coordinateSnapshot.child("longitude").getValue(Double.class);
                    if (latitude != null && longitude != null) {
                        Point point = new Point(latitude, longitude);
                        points.add(point);
                        mapObjectCollection.addPlacemark(point);
                    }
                }
                mapView.getMap().move(
                        new CameraPosition(points.get(points.size()-1), 14.0f, 0.0f, 0.0f),
                        new Animation(Animation.Type.SMOOTH, 3),
                        null);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Обработать ошибку
            }
        });
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