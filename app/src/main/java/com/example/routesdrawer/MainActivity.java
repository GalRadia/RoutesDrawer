package com.example.routesdrawer;

import android.Manifest;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.Toast;

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
import com.example.routesdrawer.databinding.ActivityMainBinding;
import com.example.routesdrawerlibrary.Models.Route;
import com.example.routesdrawerlibrary.RoutesManager;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.model.LatLng;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

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
        });
    }

    private void initUI() {
        startBTN.setOnClickListener(v -> {
                routesManager.startServiceIntent(this);

        });

        stopBTN.setOnClickListener(v -> routesManager.stopServiceIntent(this));

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
        routeAdapter.setRouteCallbacks(new RouteAdapter.RouteCallbacks() {
            @Override
            public void onRouteClicked(Route route) {
                mapView.getMapAsync(googleMap -> {
                    googleMap.clear();
                    LatLng specificLocation = new LatLng(route.getLocations().get(0).getLat(), route.getLocations().get(0).getLon());
                    googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(specificLocation, 17));
                    route.addPolyline(googleMap);
                });
            }

            @Override
            public void onBitmapClicked(Route route) {
                mapView.getMapAsync(googleMap -> {
                    route.getBitmap(googleMap, bitmap -> routesManager.saveImage(bitmap, route.getName()));
                });

            }
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


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        routesManager.handlePermissionsResult(requestCode, permissions, grantResults);
    }

}
