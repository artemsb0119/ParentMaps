package com.example.parentmaps;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.yandex.mapkit.Animation;
import com.yandex.mapkit.GeoObject;
import com.yandex.mapkit.MapKit;
import com.yandex.mapkit.MapKitFactory;
import com.yandex.mapkit.geometry.Point;
import com.yandex.mapkit.layers.GeoObjectTapEvent;
import com.yandex.mapkit.layers.GeoObjectTapListener;
import com.yandex.mapkit.map.CameraPosition;
import com.yandex.mapkit.map.GeoObjectSelectionMetadata;
import com.yandex.mapkit.map.InputListener;
import com.yandex.mapkit.map.Map;
import com.yandex.mapkit.map.MapObjectCollection;
import com.yandex.mapkit.mapview.MapView;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AddLocationActivity extends AppCompatActivity implements GeoObjectTapListener, InputListener {

    private final String MAPKIT_API_KEY = "4659ffc0-0584-4295-9bb3-a57a4321726e";
    private MapView mapView;
    private MapObjectCollection mapObjectCollection;
    private MapKit mapKit;

    private FirebaseAuth auth;
    private String childId;
    private Point selectedLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        MapKitFactory.setApiKey(MAPKIT_API_KEY);
        MapKitFactory.initialize(this);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);
        childId = getIntent().getStringExtra("user");

        mapView = findViewById(R.id.mapview);
        mapKit = MapKitFactory.getInstance();
        mapObjectCollection = mapView.getMap().getMapObjects().addCollection();
        mapView.getMap().addTapListener(this);
        mapView.getMap().addInputListener(this);
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
        return new Intent(context, AddLocationActivity.class);
    }

    @Override
    public boolean onObjectTap(@NonNull GeoObjectTapEvent geoObjectTapEvent) {
        final GeoObjectSelectionMetadata selectionMetadata = geoObjectTapEvent
                .getGeoObject()
                .getMetadataContainer()
                .getItem(GeoObjectSelectionMetadata.class);

        if (selectionMetadata != null) {
            mapView.getMap().selectGeoObject(selectionMetadata.getId(), selectionMetadata.getLayerId());
            GeoObject selectedObject = geoObjectTapEvent.getGeoObject();
            selectedLocation = selectedObject.getGeometry().get(0).getPoint();
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("Добавить точку для отслеживания?");
            builder.setPositiveButton("Да", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    getAddressFromLocation(selectedLocation.getLatitude(), selectedLocation.getLongitude());
                }
            });
            builder.setNegativeButton("Отмена", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    dialog.dismiss();
                }
            });
            AlertDialog dialog = builder.create();
            dialog.show();
        }
        return selectionMetadata != null;

    }

    @Override
    public void onMapTap(@NonNull Map map, @NonNull Point point) {
        mapView.getMap().deselectGeoObject();
    }

    @Override
    public void onMapLongTap(@NonNull Map map, @NonNull Point point) {
        mapView.getMap().deselectGeoObject();
        mapObjectCollection.addPlacemark(point);
        selectedLocation = point;
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Добавить точку для отслеживания?");
        builder.setPositiveButton("Да", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                getAddressFromLocation(selectedLocation.getLatitude(), selectedLocation.getLongitude());
            }
        });
        builder.setNegativeButton("Отмена", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void getAddressFromLocation(double latitude, double longitude) {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        auth = FirebaseAuth.getInstance();
        try {
            List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
            if (addresses != null && !addresses.isEmpty()) {
                Address address = addresses.get(0);
                String addressString = address.getAddressLine(0);
                DatabaseReference databaseReferenceAdress = FirebaseDatabase.getInstance().getReference("Notificate").child(childId).child("Adress");
                databaseReferenceAdress.push().setValue(addressString);
                DatabaseReference databaseReferenceLocate = FirebaseDatabase.getInstance().getReference("Notificate").child(childId).child("Locate");
                databaseReferenceLocate.push().setValue(selectedLocation);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}