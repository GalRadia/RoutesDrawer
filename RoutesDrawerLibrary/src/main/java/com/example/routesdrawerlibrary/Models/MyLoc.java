package com.example.routesdrawerlibrary.Models;

import com.google.android.gms.maps.model.LatLng;

public class MyLoc {

    private double lat;
    private double lon;
    private double altitude;
    private float speed; // kmh
    private float bearing;

    public MyLoc() {}

    public double getLat() {
        return lat;
    }

    public MyLoc setLat(double lat) {
        this.lat = lat;
        return this;
    }

    public double getLon() {
        return lon;
    }

    public MyLoc setLon(double lon) {
        this.lon = lon;
        return this;
    }

    public double getAltitude() {
        return altitude;
    }

    public MyLoc setAltitude(double altitude) {
        this.altitude = altitude;
        return this;
    }
    public LatLng getLatLng() {
        return new LatLng(lat, lon);
    }

    public float getSpeed() {
        return speed;
    }

    public MyLoc setSpeed(float speed) {
        this.speed = speed;
        return this;
    }

    public float getBearing() {
        return bearing;
    }

    public MyLoc setBearing(float bearing) {
        this.bearing = bearing;
        return this;
    }
}
