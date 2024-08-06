package com.example.routesdrawer;

import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import android.Manifest;
import android.content.pm.PackageManager;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.routesdrawer.Adapters.RouteAdapter;
import com.example.routesdrawer.Models.Route;
import com.example.routesdrawer.databinding.ActivityMainBinding;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.button.MaterialButton;

public class MainActivity extends AppCompatActivity {
    private static final int PERMISSION_REQUEST_CODE = 100;
    private ActivityMainBinding binding;
   private Button startBTN;
   private Button stopBTN;
   private Button refreshBTN;
   private RecyclerView recyclerView;
   private MapView mapView;
   private RoutesManager routesManager;
   private RouteAdapter routeAdapter;
    private static final String MAPVIEW_BUNDLE_KEY = "MapViewBundleKey";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setupUI();
        initUI();
        initMap(savedInstanceState);

    }

    private void initMap(Bundle savedInstanceState) {
        Bundle mapViewBundle = null;
        if (savedInstanceState != null) {
            mapViewBundle = savedInstanceState.getBundle(MAPVIEW_BUNDLE_KEY);
        }
        mapView.onCreate(mapViewBundle);

        mapView.getMapAsync(googleMap -> {
            googleMap.getUiSettings().setMyLocationButtonEnabled(false);
            googleMap.getUiSettings().setZoomControlsEnabled(true);
            googleMap.getUiSettings().setCompassEnabled(true);
            googleMap.getUiSettings().setMapToolbarEnabled(true);
            googleMap.getUiSettings().setZoomGesturesEnabled(true);
            googleMap.getUiSettings().setScrollGesturesEnabled(true);
            googleMap.getUiSettings().setTiltGesturesEnabled(true);
            googleMap.getUiSettings().setRotateGesturesEnabled(true);
            LatLng specificLocation = new LatLng(32.11504612996519, 34.81780814048655);
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(specificLocation, 17));

            // Replace with your desired coordinates

        });
    }

    private void initUI() {
        startBTN.setOnClickListener(v -> {
            // Start the service
            if (ContextCompat.checkSelfPermission(
                    this, Manifest.permission.ACCESS_FINE_LOCATION) ==
                    PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(
                            this, Manifest.permission.ACCESS_COARSE_LOCATION) ==
                            PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(
                            this, Manifest.permission.FOREGROUND_SERVICE) ==
                            PackageManager.PERMISSION_GRANTED &&
                    (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
                            ContextCompat.checkSelfPermission(
                                    this, Manifest.permission.FOREGROUND_SERVICE_LOCATION) ==
                                    PackageManager.PERMISSION_GRANTED)) {

                // All necessary permissions are granted, proceed with the action
                routesManager.startServiceIntent(this);
            } else {
                // Check if we should show an educational UI
                if (ActivityCompat.shouldShowRequestPermissionRationale(
                        this, Manifest.permission.ACCESS_FINE_LOCATION) ||
                        ActivityCompat.shouldShowRequestPermissionRationale(
                                this, Manifest.permission.ACCESS_COARSE_LOCATION) ||
                        ActivityCompat.shouldShowRequestPermissionRationale(
                                this, Manifest.permission.FOREGROUND_SERVICE) ||
                        (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                                ActivityCompat.shouldShowRequestPermissionRationale(
                                        this, Manifest.permission.FOREGROUND_SERVICE_LOCATION))) {

                    // Show an educational UI to explain why the permissions are needed
                } else {
                    // Directly request the permissions
                    String[] permissions;
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        permissions = new String[] {
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION,
                                Manifest.permission.FOREGROUND_SERVICE,
                                Manifest.permission.FOREGROUND_SERVICE_LOCATION
                        };
                    } else {
                        permissions = new String[] {
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION,
                                Manifest.permission.FOREGROUND_SERVICE
                        };
                    }

                    ActivityCompat.requestPermissions(this, permissions, PERMISSION_REQUEST_CODE);
                }
            }
        });

        stopBTN.setOnClickListener(v -> {
            // Stop the service
            routesManager.stopServiceIntent(this);
        });
        refreshBTN.setOnClickListener(v -> {
            routeAdapter.setRoutes(routesManager.getRoutes());
            routeAdapter.notifyDataSetChanged();
        });

    }

    private void setupUI() {
        startBTN = binding.StartBTN;
        stopBTN = binding.StopBTN;
        recyclerView = binding.recyclerView;
        mapView = binding.mapView;
        refreshBTN = binding.RefreshBTN;
        routesManager = RoutesManager.getInstance(this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        routeAdapter = new RouteAdapter(this, routesManager.getRoutes());
        routeAdapter.setRouteCallbacks(route -> {
            mapView.getMapAsync(googleMap -> {
                googleMap.clear();
                LatLng specificLocation = new LatLng(route.getLocations().get(0).getLat(), route.getLocations().get(0).getLon());
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(specificLocation, 17));
                route.addPolyline(googleMap);

            });
            // Handle route click

        });
        recyclerView.setAdapter(routeAdapter);

    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        Bundle mapViewBundle = outState.getBundle(MAPVIEW_BUNDLE_KEY);
        if (mapViewBundle == null) {
            mapViewBundle = new Bundle();
            outState.putBundle(MAPVIEW_BUNDLE_KEY, mapViewBundle);
        }

        mapView.onSaveInstanceState(mapViewBundle);

    }



    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    public void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
        mapView.onStop();
    }
    @Override
    public void onPause() {
        mapView.onPause();
        super.onPause();
    }

    @Override
    public void onDestroy() {
        mapView.onDestroy();
        super.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }



}