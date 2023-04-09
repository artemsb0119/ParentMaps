package com.example.parentmaps;

import static java.security.AccessController.getContext;

import android.Manifest;
import android.app.Activity;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PointF;
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
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.google.firebase.auth.FirebaseUser;
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


import com.yandex.mapkit.geometry.Geometry;
import com.yandex.mapkit.geometry.Point;
import com.yandex.mapkit.layers.GeoObjectTapEvent;
import com.yandex.mapkit.layers.GeoObjectTapListener;
import com.yandex.mapkit.layers.ObjectEvent;
import com.yandex.mapkit.map.CameraListener;
import com.yandex.mapkit.map.CameraPosition;
import com.yandex.mapkit.map.CameraUpdateReason;
import com.yandex.mapkit.map.GeoObjectSelectionMetadata;
import com.yandex.mapkit.map.InputListener;
import com.yandex.mapkit.map.Map;
import com.yandex.mapkit.map.MapObject;
import com.yandex.mapkit.map.MapObjectCollection;
import com.yandex.mapkit.map.MapObjectTapListener;
import com.yandex.mapkit.map.PlacemarkMapObject;
import com.yandex.mapkit.map.PolylineMapObject;
import com.yandex.mapkit.mapview.MapView;
import com.yandex.mapkit.transport.masstransit.PedestrianRouter;
import com.yandex.mapkit.user_location.UserLocationLayer;
import com.yandex.mapkit.user_location.UserLocationObjectListener;
import com.yandex.mapkit.user_location.UserLocationView;
import com.yandex.runtime.Error;
import com.yandex.runtime.image.ImageProvider;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;


public class MapsActivity extends AppCompatActivity implements GeoObjectTapListener, InputListener, DrivingSession.DrivingRouteListener {

    private final String MAPKIT_API_KEY = "4659ffc0-0584-4295-9bb3-a57a4321726e";

    private MapsViewModel viewModel;

    private MapView mapView;
    private MapKit mapKit;
    private DrivingRouter drivingRouter;
    private DrivingSession drivingSession;
    private LocationManager locationManager;
    private Location location;
    private Point currentLocation;
    private Point selectedLocation = new Point(0, 0);
    boolean isReachedDestination = false;
    private MapObjectCollection mapObjectCollection;
    private ArrayList<Point> routePoints = new ArrayList<>();
    private double distanceToRoute = 0;
    private long lastNotificationTime = 0;
    private UserLocationLayer locationLayer;

    private Handler handler;
    private Runnable runnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        MapKitFactory.setApiKey(MAPKIT_API_KEY);

        MapKitFactory.initialize(this);
        // Создание MapView.
        setContentView(R.layout.activity_maps);
        super.onCreate(savedInstanceState);
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
                if (location != null && (location.getAccuracy() < 100 || System.currentTimeMillis() - location.getTime() < 10000)) {
                    currentLocation = new Point(location.getLatitude(), location.getLongitude());
                    mapView.getMap().move(
                            new CameraPosition(currentLocation, 14.0f, 0.0f, 0.0f),
                            new Animation(Animation.Type.SMOOTH, 3),
                            null);
                    double distance = distanceBetweenPoints(currentLocation, selectedLocation);
                    Log.d("MapsActivity", String.valueOf(distance));
                    if (routePoints.size() > 0) {
                        distanceToRoute = distanceBetweenPoints(currentLocation, getNearestPointOnRoute(currentLocation));
                        Log.d("MapsActivity", String.valueOf("marshrut " + distanceToRoute));
                    }
                    if (distance < 0.02 && !isReachedDestination) {
                        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), "channel_id")
                                .setSmallIcon(R.drawable.icon_map)
                                .setContentTitle("Вы прибыли")
                                .setContentText("Вы достигли конечной точки маршрута.")
                                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

                        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(getApplicationContext());
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            NotificationChannel channel = new NotificationChannel("channel_id", "Channel name", NotificationManager.IMPORTANCE_DEFAULT);
                            notificationManager.createNotificationChannel(channel);
                        }

                        notificationManager.notify(1, builder.build());

                        isReachedDestination = true;

                        mapObjectCollection.clear();
                        distanceToRoute = 0;
                    } else if (distanceToRoute > 1) {
                        if (System.currentTimeMillis() - lastNotificationTime > 180000) {
                            NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), "channel_id")
                                    .setSmallIcon(R.drawable.icon_map)
                                    .setContentTitle("Вы отклонились от маршрута")
                                    .setContentText("Пожалуйста, вернитесь на маршрут.")
                                    .setPriority(NotificationCompat.PRIORITY_DEFAULT);

                            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(getApplicationContext());
                            // уведомление будет показано только если приложение находится в фоновом режиме
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                NotificationChannel channel = new NotificationChannel("channel_id", "Channel name", NotificationManager.IMPORTANCE_DEFAULT);
                                notificationManager.createNotificationChannel(channel);
                            }

                            notificationManager.notify(1, builder.build());
                            lastNotificationTime = System.currentTimeMillis();
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
            locationManager.requestLocationUpdates(provider, 100, 10, locationListener);
        }

        Location location = locationManager.getLastKnownLocation(provider);
        if (location != null) {
            currentLocation = new Point(location.getLatitude(), location.getLongitude());
            // Перемещение камеры к текущему местоположению пользователя.
            mapView.getMap().move(
                    new CameraPosition(currentLocation, 14.0f, 0.0f, 0.0f),
                    new Animation(Animation.Type.SMOOTH, 3),
                    null);
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
        // Вызов onStop нужно передавать инстансам MapView и MapKit.
        mapView.onStop();
        MapKitFactory.getInstance().onStop();
        super.onStop();
    }

    @Override
    protected void onStart() {
        // Вызов onStart нужно передавать инстансам MapView и MapKit.
        super.onStart();
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
                    // Если пользователь подтвердил, строим маршрут
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
            // Добавление каждой промежуточной точки маршрута в коллекцию
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
                    handler.postDelayed(this, 60000); // 60,000 миллисекунд = 1 минута
                }
            }
        };
        handler.postDelayed(runnable, 60000);
    }

    private void getAddressFromLocation(double latitude, double longitude) {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
            if (addresses != null && !addresses.isEmpty()) {
                Address address = addresses.get(0);
                Log.d("MapsActivity", "Current address: " + address.getAddressLine(0));
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
        getMenuInflater().inflate(R.menu.child_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
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