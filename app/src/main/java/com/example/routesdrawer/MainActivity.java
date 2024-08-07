package com.example.routesdrawer;

import android.Manifest;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.routesdrawer.Adapters.RouteAdapter;
import com.example.routesdrawer.Models.Route;
import com.example.routesdrawer.databinding.ActivityMainBinding;
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
            if (hasAllPermissions()) {
                routesManager.startServiceIntent(this);
            } else {
                requestPermissions();
            }
        });

        stopBTN.setOnClickListener(v -> routesManager.stopServiceIntent(this));

        refreshBTN.setOnClickListener(v -> {
            routeAdapter.setRoutes(routesManager.getRoutes());
            routeAdapter.notifyDataSetChanged();
        });
    }

    private boolean hasAllPermissions() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.FOREGROUND_SERVICE) == PackageManager.PERMISSION_GRANTED &&
                (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU || ContextCompat.checkSelfPermission(this, Manifest.permission.FOREGROUND_SERVICE_LOCATION) == PackageManager.PERMISSION_GRANTED);
    }

    private void requestPermissions() {
        String[] permissions;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions = new String[] {
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
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
                    route.getBitmap(googleMap, bitmap -> saveImage(bitmap, route.getName()));
                });

            }
        });
        recyclerView.setAdapter(routeAdapter);
    }

    private void saveImage(Bitmap finalBitmap, String imageName) {
        String fileName = "Image-" + imageName + ".jpg";
        OutputStream outStream;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ContentResolver resolver = getContentResolver();
            ContentValues contentValues = new ContentValues();
            contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, fileName);
            contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg");
            contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES);

            Uri imageUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);
            try {
                if (imageUri != null) {
                    outStream = resolver.openOutputStream(imageUri);
                    finalBitmap.compress(Bitmap.CompressFormat.JPEG, 90, outStream);
                    if (outStream != null) {
                        outStream.flush();
                        outStream.close();
                        Toast.makeText(this, "Image saved successfully", Toast.LENGTH_SHORT).show();
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            String root = Environment.getExternalStorageDirectory().toString();
            File myDir = new File(root + "/Pictures");
            myDir.mkdirs();
            File file = new File(myDir, fileName);
            if (file.exists()) file.delete();
            try {
                outStream = new FileOutputStream(file);
                finalBitmap.compress(Bitmap.CompressFormat.JPEG, 90, outStream);
                outStream.flush();
                outStream.close();
                Toast.makeText(this, "Image saved successfully", Toast.LENGTH_SHORT).show();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
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
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permissions granted, proceed with the action
            } else {
                // Permissions denied, show a message to the user
                Toast.makeText(this, "Permissions denied, cannot proceed", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
