package com.example.routesdrawer;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.routesdrawer.Models.Route;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class RoutesManager {
    private static final String PREFS_NAME = "routes_prefs";
    private static final String ROUTES_KEY = "routes_key";

    private List<Route> routes;
    private SharedPreferences sharedPreferences;
    private Gson gson;

    public RoutesManager(Context context) {
        sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        gson = new Gson();
        routes = loadRoutes();
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
}
