package com.example.routesdrawerlibrary;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.widget.Toast;

import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKey;

import com.example.routesdrawerlibrary.Models.MyLoc;
import com.example.routesdrawerlibrary.Models.Route;
import com.example.routesdrawerlibrary.Services.LocationService;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.lang.reflect.Type;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.List;

public class RoutesManager {
    private static final String PREFS_NAME = "routes_prefs";
    private static final String ROUTES_KEY = "routes_key";
    private static final String CURRENT_ROUTE_KEY = "current_route_key";

    private List<Route> routes;
    private Route currentRoute;
    private SharedPreferences sharedPreferences;
    private Gson gson;
    private Context context;

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



}
