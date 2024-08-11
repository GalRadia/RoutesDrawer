package com.example.routesdrawerlibrary;

import android.Manifest;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKey;

import com.example.routesdrawerlibrary.Models.MyLoc;
import com.example.routesdrawerlibrary.Models.Route;
import com.example.routesdrawerlibrary.Services.LocationService;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Type;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.List;

public class RoutesManager {
    private static final String PREFS_NAME = "routes_prefs";
    private static final String ROUTES_KEY = "routes_key";
    private static final String CURRENT_ROUTE_KEY = "current_route_key";
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 100;

    private List<Route> routes;
    private Route currentRoute;
    private SharedPreferences sharedPreferences;
    private Gson gson;
    private Context context;
    private static final int PERMISSION_REQUEST_CODE = 100;

    private static RoutesManager instance;

    public Context getContext() {
        return context;
    }

    private RoutesManager(Context context) {
        try {
            MasterKey masterKey = new MasterKey.Builder(context)
                    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                    .build();

            sharedPreferences = EncryptedSharedPreferences.create(
                    context,
                    PREFS_NAME,
                    masterKey,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            );

        } catch (GeneralSecurityException | IOException e) {
            e.printStackTrace();
            // Handle the exception
        }
        this.context = context;
        gson = new Gson();
        routes = loadRoutes();
        currentRoute = loadCurrentRoute();
        if (currentRoute == null) {
            currentRoute = new Route(new ArrayList<>(), "Route " + (routes.size() + 1));
        }
    }

    public static synchronized RoutesManager getInstance(Context context) {
        if (instance == null) {
            instance = new RoutesManager(context);
        }
        return instance;
    }

    public List<Route> getRoutes() {
        return routes;
    }

    public void addRoute(Route route) {
        routes.add(route);
        saveRoutes();
    }

    public void removeRoute(Route route) {
        routes.remove(route);
        saveRoutes();
    }

    public void clearRoutes() {
        routes.clear();
        saveRoutes();
    }

    public void addLocation(MyLoc location) {
        currentRoute.getLocations().add(location);
        saveCurrentRoute();
    }

    public void completeCurrentRoute() {
        if (!currentRoute.getLocations().isEmpty()) {
            addRoute(currentRoute);
            currentRoute = new Route(new ArrayList<>(), "Route " + (routes.size() + 1));
            saveCurrentRoute();
        }
    }

    private void saveRoutes() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        String json = gson.toJson(routes);
        editor.putString(ROUTES_KEY, json);
        editor.apply();
    }

    private List<Route> loadRoutes() {
        String json = sharedPreferences.getString(ROUTES_KEY, null);
        if (json == null) {
            return new ArrayList<>();
        }
        Type type = new TypeToken<List<Route>>() {}.getType();
        return gson.fromJson(json, type);
    }

    private void saveCurrentRoute() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        String json = gson.toJson(currentRoute);
        editor.putString(CURRENT_ROUTE_KEY, json);
        editor.apply();
    }

    private Route loadCurrentRoute() {
        String json = sharedPreferences.getString(CURRENT_ROUTE_KEY, null);
        if (json == null) {
            return null;
        }
        return gson.fromJson(json, Route.class);
    }

    public void initializeNewRouteIfNeeded() {
        if (currentRoute == null || currentRoute.getLocations().isEmpty()) {
            currentRoute = new Route(new ArrayList<>(), "Route " + (routes.size() + 1));
            saveCurrentRoute();
        }
    }

    public void startServiceIntent(Context context){
        if (!checkPermissions()) {
            requestPermissions((Activity) context);
            return;
        }
        initializeNewRouteIfNeeded();
        Intent serviceIntent = new Intent(context, LocationService.class);
        serviceIntent.setAction(LocationService.START_FOREGROUND_SERVICE);
        context.startService(serviceIntent);
        Toast.makeText(context, "Service is running", Toast.LENGTH_SHORT).show();
    }

    public void stopServiceIntent(Context context) {
        Intent serviceIntent = new Intent(context, LocationService.class);
        serviceIntent.setAction(LocationService.STOP_FOREGROUND_SERVICE);
        context.startService(serviceIntent); // Start the service with the stop action
        Toast.makeText(context, "Service is stopped", Toast.LENGTH_SHORT).show();
    }
    public void saveImage(Bitmap finalBitmap, String imageName) {
        String fileName = "Image-" + imageName + ".jpg";
        OutputStream outStream;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ContentResolver resolver = getContext().getContentResolver();
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
                        Toast.makeText(getContext(), "Image saved successfully", Toast.LENGTH_SHORT).show();
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
                Toast.makeText(getContext(), "Image saved successfully", Toast.LENGTH_SHORT).show();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    public boolean checkPermissions() {
        return ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(context, android.Manifest.permission.FOREGROUND_SERVICE) == PackageManager.PERMISSION_GRANTED&&
                ContextCompat.checkSelfPermission(context, android.Manifest.permission.FOREGROUND_SERVICE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    public void requestPermissions(Activity activity) {
        ActivityCompat.requestPermissions(activity,
                new String[]{
                        android.Manifest.permission.ACCESS_FINE_LOCATION,
                        android.Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.FOREGROUND_SERVICE,
                        Manifest.permission.FOREGROUND_SERVICE_LOCATION,
                },
                LOCATION_PERMISSION_REQUEST_CODE);
    }


    public void handlePermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            boolean allGranted = true;
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    allGranted = false;
                    break;
                }
            }
            if (allGranted) {
                // All permissions granted, proceed with location-related operations
                startServiceIntent(context);
            } else {
                // Handle the case where permissions are not granted
                // You might want to show a message to the user or take other actions
            }
        }
    }



}
