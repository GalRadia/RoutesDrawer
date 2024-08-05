package com.example.routesdrawer.Models;

import android.graphics.Color;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.List;

public class Route {
    private List<MyLoc> locations;
    private String name;
    private GoogleMap map;


    public Route() {
    }

    public Route(List<MyLoc> locations, String name,GoogleMap map) {
        this.locations = locations;
        this.name = name;
        this.map = map;
    }

    public List<MyLoc> getLocations() {
        return locations;
    }

    public Route setLocations(List<MyLoc> locations) {
        this.locations = locations;
        return this;
    }

    public String getName() {
        return name;
    }

    public Route setName(String name) {
        this.name = name;
        return this;
    }

    public GoogleMap getMap() {
        return map;
    }

    public Route setMap(GoogleMap map) {
        this.map = map;
        return this;
    }
    public void addPolyline(){
        if (map == null || locations == null || locations.isEmpty()) {
            return;
        }

        for (int i = 0; i < locations.size() - 1; i++) {
            MyLoc startLoc = locations.get(i);
            MyLoc endLoc = locations.get(i + 1);

            LatLng startLatLng = new LatLng(startLoc.getLat(), startLoc.getLon());
            LatLng endLatLng = new LatLng(endLoc.getLat(), endLoc.getLon());

            float speed = startLoc.getSpeed(); // Assuming speed is in km/h
            int color = getColorForSpeed(speed);

            PolylineOptions polylineOptions = new PolylineOptions()
                    .add(startLatLng, endLatLng)
                    .color(color)
                    .width(5); // Set the width of the polyline

            map.addPolyline(polylineOptions);
        }
    }
    private int getColorForSpeed(float speed) {
        // Assuming speed ranges from 0 to 100 km/h, with faster speeds being redder
        float normalizedSpeed = Math.min(speed / 100, 1.0f); // Normalize speed to 0-1
        int red = (int) (255 * normalizedSpeed);
        int green = (int) (255 * (1 - normalizedSpeed));
        int blue = 0;

        return Color.rgb(red, green, blue);
    }
}
