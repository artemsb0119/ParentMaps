package com.example.parentmaps;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

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
import com.yandex.mapkit.RequestPoint;
import com.yandex.mapkit.RequestPointType;
import com.yandex.mapkit.directions.DirectionsFactory;
import com.yandex.mapkit.directions.driving.DrivingOptions;
import com.yandex.mapkit.directions.driving.DrivingRoute;
import com.yandex.mapkit.directions.driving.DrivingRouter;
import com.yandex.mapkit.directions.driving.DrivingSession;
import com.yandex.mapkit.directions.driving.VehicleOptions;


import com.yandex.mapkit.geometry.Point;
import com.yandex.mapkit.layers.GeoObjectTapEvent;
import com.yandex.mapkit.layers.GeoObjectTapListener;
import com.yandex.mapkit.map.CameraPosition;
import com.yandex.mapkit.map.GeoObjectSelectionMetadata;
import com.yandex.mapkit.map.InputListener;
import com.yandex.mapkit.map.Map;
import com.yandex.mapkit.map.MapObjectCollection;
import com.yandex.mapkit.map.PolylineMapObject;
import com.yandex.mapkit.mapview.MapView;
import com.yandex.mapkit.user_location.UserLocationLayer;
import com.yandex.runtime.Error;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;


public class MapsActivity extends AppCompatActivity implements GeoObjectTapListener, InputListener, DrivingSession.DrivingRouteListener {

    private SharedPreferences sharedPreferences;
    private final String MAPKIT_API_KEY = "4659ffc0-0584-4295-9bb3-a57a4321726e";

    private MapsViewModel viewModel;

    private MapView mapView;
    private MapKit mapKit;
    private DrivingRouter drivingRouter;
    private DrivingSession drivingSession;
    private LocationManager locationManager;
    private Point currentLocation;
    private Point selectedLocation = new Point(0, 0);
    boolean isReachedDestination = false;
    boolean isReachedPoint = false;
    private MapObjectCollection mapObjectCollection;
    private ArrayList<Point> routePoints = new ArrayList<>();
    private double distanceToRoute = 0;
    private long lastNotificationTime = 0;
    private UserLocationLayer locationLayer;
    private String lastAddress = "";

    private FirebaseAuth auth;

    private Handler handler;
    private Runnable runnable;
    private FirebaseDatabase database;
    private DatabaseReference notificateReference;
    private DatabaseReference friendsReference;
    private List<Point> points;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        Intent locationServiceIntent = new Intent(this, LocationService.class);
        stopService(locationServiceIntent);

        MapKitFactory.setApiKey(MAPKIT_API_KEY);
        MapKitFactory.initialize(this);

        setContentView(R.layout.activity_maps);
        super.onCreate(savedInstanceState);
        points = new ArrayList<>();
        auth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = auth.getCurrentUser();
        String currentUserId = currentUser.getUid();
        database = FirebaseDatabase.getInstance();
        notificateReference = database.getReference("Notificate").child(currentUserId);
        notificateReference.child("Locate").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot coordinateSnapshot : snapshot.getChildren()) {
                    Double latitude = coordinateSnapshot.child("latitude").getValue(Double.class);
                    Double longitude = coordinateSnapshot.child("longitude").getValue(Double.class);
                    if (latitude != null && longitude != null) {
                        Point point = new Point(latitude, longitude);
                        points.add(point);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        viewModel = new ViewModelProvider(this).get(MapsViewModel.class);
        observeViewModel();
        mapView = (MapView) findViewById(R.id.mapview);
        requestLocationPermission();
        mapKit = MapKitFactory.getInstance();
        locationLayer = mapKit.createUserLocationLayer(mapView.getMapWindow());
        locationLayer.setVisible(true);

        LocationListener locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                if (location != null && (location.getAccuracy() < 100)) {
                    currentLocation = new Point(location.getLatitude(), location.getLongitude());
                    mapView.getMap().move(
                            new CameraPosition(currentLocation, 14.0f, 0.0f, 0.0f),
                            new Animation(Animation.Type.SMOOTH, 3),
                            null);
                    double distance = distanceBetweenPoints(currentLocation, selectedLocation);

                    for (Point point:points) {
                        double distancePoint = distanceBetweenPoints(currentLocation, point);
                        if (distancePoint <0.2 && !isReachedPoint) {
                            isReachedPoint = true;
                            friendsReference = database.getReference("SendNotificate").child(currentUserId);
                            friendsReference.setValue("Пользователь достиг точки");
                        }
                        if (distancePoint >0.5 && isReachedPoint) {
                            isReachedPoint = false;
                            friendsReference = database.getReference("SendNotificate").child(currentUserId);
                            friendsReference.setValue("Пользователь отдалился от точки");
                        }
                    }

                    if (routePoints.size() > 0) {
                        distanceToRoute = distanceBetweenPoints(currentLocation, getNearestPointOnRoute(currentLocation));
                        Log.d("MapsActivity", String.valueOf("marshrut " + distanceToRoute));
                        if (distance < 0.02 && !isReachedDestination) {
                            friendsReference = database.getReference("SendNotificate").child(currentUserId);
                            friendsReference.setValue("Пользователь достиг конечной точки");

                            isReachedDestination = true;

                            mapObjectCollection.clear();
                            distanceToRoute = 0;
                        } else if (distanceToRoute > 1) {
                            if (System.currentTimeMillis() - lastNotificationTime > 180000) {
                                friendsReference = database.getReference("SendNotificate").child(currentUserId);
                                friendsReference.setValue("Пользователь отклонился от маршрута");
                            }
                        }
                    }
                }
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {
            }

            @Override
            public void onProviderEnabled(String provider) {
            }

            @Override
            public void onProviderDisabled(String provider) {
            }
        };

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        criteria.setPowerRequirement(Criteria.POWER_HIGH);
        criteria.setAltitudeRequired(true);
        criteria.setBearingRequired(true);
        criteria.setSpeedRequired(true);
        String provider = locationManager.getBestProvider(criteria, true);

        if (provider != null) {
            locationManager.requestLocationUpdates(provider, 1000, 10, locationListener);
            Location location = locationManager.getLastKnownLocation(provider);
            if (location != null) {
                currentLocation = new Point(location.getLatitude(), location.getLongitude());
                mapView.getMap().move(
                        new CameraPosition(currentLocation, 14.0f, 0.0f, 0.0f),
                        new Animation(Animation.Type.SMOOTH, 3),
                        null);
            }
        }

        mapView.getMap().addTapListener(this);
        mapView.getMap().addInputListener(this);
        mapObjectCollection = mapView.getMap().getMapObjects().addCollection();
        drivingRouter = DirectionsFactory.getInstance().createDrivingRouter();
        startGettingAddress();
    }

    private void requestLocationPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 0);
        }
    }

    @Override
    protected void onStop() {
        mapView.onStop();
        MapKitFactory.getInstance().onStop();
        Log.d("MapsActivity", "destroy");
        Intent locationServiceIntent = new Intent(this, LocationService.class);
        sharedPreferences =getSharedPreferences("my_preferences", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("lastAddress", lastAddress);
        ContextCompat.startForegroundService(this,locationServiceIntent);
        super.onStop();
    }

    @Override
    protected void onStart() {
        super.onStart();
        Intent locationServiceIntent = new Intent(this, LocationService.class);
        stopService(locationServiceIntent);
        MapKitFactory.getInstance().onStart();
        mapView.onStart();
    }

    public static Intent newIntent(Context context) {
        return new Intent(context, MapsActivity.class);
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
            builder.setMessage("Построить маршрут до выбранной точки?");
            builder.setPositiveButton("Да", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    mapObjectCollection.clear();
                    isReachedDestination = false;
                    sumbitRequest();
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
        builder.setMessage("Построить маршрут до выбранной точки?");
        builder.setPositiveButton("Да", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                mapObjectCollection.clear();
                isReachedDestination = false;
                sumbitRequest();
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

    @Override
    public void onDrivingRoutes(@NonNull List<DrivingRoute> list) {
        routePoints.clear();
        for (DrivingRoute route : list) {

            PolylineMapObject polylineMapObject = mapObjectCollection.addPolyline(route.getGeometry());
            polylineMapObject.setStrokeColor(Color.GREEN);
            routePoints.addAll(route.getGeometry().getPoints());
        }
    }

    @Override
    public void onDrivingRoutesError(@NonNull Error error) {
        String errorMessage = "Неизвестная ошибка!";
        Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show();
    }

    private void sumbitRequest() {
        DrivingOptions drivingOptions = new DrivingOptions();
        VehicleOptions vehicleOptions = new VehicleOptions();
        ArrayList<RequestPoint> requestPoints = new ArrayList<RequestPoint>();
        if (currentLocation != null) {
            requestPoints.add(new RequestPoint(currentLocation, RequestPointType.WAYPOINT, null));
            requestPoints.add(new RequestPoint(selectedLocation, RequestPointType.WAYPOINT, null));
            drivingSession = drivingRouter.requestRoutes(requestPoints, drivingOptions, vehicleOptions, this);
        }
    }

    private void startGettingAddress() {
        handler = new Handler();
        runnable = new Runnable() {
            @Override
            public void run() {
                if (currentLocation != null) {
                    getAddressFromLocation(currentLocation.getLatitude(), currentLocation.getLongitude());
                    handler.postDelayed(this, 300000);
                }
            }
        };
        handler.postDelayed(runnable, 300000);
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
                if (lastAddress.equals("")||!lastAddress.equals(address.getAddressLine(0))){
                    lastAddress = address.getAddressLine(0);
                    Log.d("iii","main");
                    DateFormat dateFormat = new SimpleDateFormat("dd.MM HH:mm", Locale.getDefault());
                    String currentTime = dateFormat.format(new Date());
                    String addressString = currentTime + " " + address.getAddressLine(0);
                    DatabaseReference databaseReferenceAdress = FirebaseDatabase.getInstance().getReference("ChildLocation").child(currentUserId).child("Adress");
                    databaseReferenceAdress.push().setValue(addressString);
                    databaseReferenceAdress.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.getChildrenCount() > 500) {
                                DataSnapshot firstChild = snapshot.getChildren().iterator().next();
                                firstChild.getRef().removeValue();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                    DatabaseReference databaseReferenceLocate = FirebaseDatabase.getInstance().getReference("ChildLocation").child(currentUserId).child("Locate");
                    databaseReferenceLocate.push().setValue(currentLocation);
                    databaseReferenceLocate.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.getChildrenCount() > 100) {
                                DataSnapshot firstChild = snapshot.getChildren().iterator().next();
                                firstChild.getRef().removeValue();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });

                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private double distanceBetweenPoints(Point p1, Point p2) {
        final int R = 6371; // Радиус земли в километрах
        double lat1 = p1.getLatitude();
        double lon1 = p1.getLongitude();
        double lat2 = p2.getLatitude();
        double lon2 = p2.getLongitude();
        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double distance = R * c;
        return distance;
    }

    private Point getNearestPointOnRoute(Point location) {
        Point nearestPoint = null;
        double minDistance = Double.MAX_VALUE;
        for (Point point : routePoints) {
            double distance = distanceBetweenPoints(location, point);
            if (distance < minDistance) {
                minDistance = distance;
                nearestPoint = point;
            }
        }
        return nearestPoint;
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.maps_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.item_Add) {
            Intent intent = RequestActivity.newIntent(MapsActivity.this);
            startActivity(intent);
        }
        if (item.getItemId() == R.id.item_Logout) {
            viewModel.logout();
        }
        return super.onOptionsItemSelected(item);
    }

    private void observeViewModel() {
        viewModel.getUser().observe(this, new Observer<FirebaseUser>() {
            @Override
            public void onChanged(FirebaseUser firebaseUser) {
                if (firebaseUser == null) {
                    Intent intent = LoginActivity.newIntent(MapsActivity.this);
                    startActivity(intent);
                    finish();
                }
            }
        });
    }
}