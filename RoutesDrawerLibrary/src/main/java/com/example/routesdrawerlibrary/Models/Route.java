package com.example.routesdrawerlibrary.Models;

import android.graphics.Bitmap;
import android.graphics.Color;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.StrokeStyle;
import com.google.android.gms.maps.model.StyleSpan;

import java.util.List;

public class Route {
    private List<MyLoc> locations;
    private String name;


    public Route() {
    }

    public Route(List<MyLoc> locations, String name) {
        this.locations = locations;
        this.name = name;
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

    public void addPolyline(GoogleMap map) {
        if (map == null || locations == null || locations.isEmpty()) {
            return;
        }

        for (int i = 0; i < locations.size() - 1; i++) {
            MyLoc startLoc = locations.get(i);
            MyLoc endLoc = locations.get(i + 1);

            LatLng startLatLng = new LatLng(startLoc.getLat(), startLoc.getLon());
            LatLng endLatLng = new LatLng(endLoc.getLat(), endLoc.getLon());

            float startSpeed = startLoc.getSpeed(); // Speed at the start of the segment
            float endSpeed = endLoc.getSpeed(); // Speed at the end of the segment

            // Interpolate the color between startSpeed and endSpeed
            int startColor = getColorForSpeed(startSpeed);
            int endColor = getColorForSpeed(endSpeed);

            // Create two polylines for a smooth color transition
            PolylineOptions polylineOptions = new PolylineOptions()
                    .add(startLatLng)
                    .add(endLatLng)
                    .addSpan(new StyleSpan(StrokeStyle.gradientBuilder(startColor, endColor).build()))
                    .width(22); // Set the width of the polyline
            map.addPolyline(polylineOptions);
        }
    }

    private int getColorForSpeed(float speed) {
        // Assuming speed ranges from 0 to 100 km/h, with faster speeds being much redder
        float normalizedSpeed = Math.min(speed / 20, 1.0f); // Normalize speed to 0-1

        // Use a power function to make the color change faster and stronger
        float exponent = 2.0f; // Adjust this value for stronger or weaker transition
        float adjustedSpeed = (float) Math.pow(normalizedSpeed, exponent);

        // Base color: mid-blue (RGB: 0, 128, 255)
        int startRed = 0;
        int startGreen = 128;
        int startBlue = 255;

        // End color: much redder (RGB: 255, 0, 0)
        int endRed = 255;
        int endGreen = 0;
        int endBlue = 0;

        int red = (int) (startRed + (endRed - startRed) * adjustedSpeed);
        int green = (int) (startGreen + (endGreen - startGreen) * adjustedSpeed);
        int blue = (int) (startBlue + (endBlue - startBlue) * adjustedSpeed);

        return Color.rgb(red, green, blue);
    }

    //    public File saveBitmap(Bitmap bitmap, Context context) {
//        String fileName = "image.png"; // or any other format
//        FileOutputStream outStream = null;
//        File file = null;
//
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
//            ContentResolver resolver = context.getContentResolver();
//            ContentValues contentValues = new ContentValues();
//            contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, fileName);
//            contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "image/png");
//            contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES);
//
//            Uri imageUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);
//            try {
//                outStream = (FileOutputStream) resolver.openOutputStream(imageUri);
//                file = new File(imageUri.getPath());
//            } catch (FileNotFoundException e) {
//                e.printStackTrace();
//            }
//        } else {
//            File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
//            file = new File(path, fileName);
//            try {
//                path.mkdirs();
//                outStream = new FileOutputStream(file);
//            } catch (FileNotFoundException e) {
//                e.printStackTrace();
//            }
//        }
//
//        if (outStream != null) {
//            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outStream);
//            try {
//                outStream.flush();
//                outStream.close();
//                Toast.makeText(context, "Image saved successfully", Toast.LENGTH_SHORT).show();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
//
//        return file;
//    }
    public void getBitmap(GoogleMap map, OnBitmapReadyCallback callback) {
        map.snapshot(bitmap -> {
            if (callback != null) {
                callback.onBitmapReady(bitmap);
            }
        });


    }

    public interface OnBitmapReadyCallback {
        void onBitmapReady(Bitmap bitmap);
    }


}
