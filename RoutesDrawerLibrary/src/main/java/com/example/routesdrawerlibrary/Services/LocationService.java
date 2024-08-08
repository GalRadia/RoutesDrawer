package com.example.routesdrawerlibrary.Services;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ServiceInfo;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Build;
import android.os.IBinder;
import android.os.Looper;
import android.os.PowerManager;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.routesdrawerlibrary.Models.MyLoc;
import com.example.routesdrawerlibrary.R;
import com.example.routesdrawerlibrary.RoutesManager;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

public class LocationService extends Service {

    public static final String START_FOREGROUND_SERVICE = "START_FOREGROUND_SERVICE";
    public static final String STOP_FOREGROUND_SERVICE = "STOP_FOREGROUND_SERVICE";
    public static final String DEFAULT_CHANNEL_NAME = "Location Service";
    public static final String DEFAULT_CHANNEL_DESCRIPTION = "Notifications for location service";

    public static int NOTIFICATION_ID = 168;
    private int lastShownNotificationId = -1;
    public static String CHANNEL_ID = "com.example.routesdrawer.CHANNEL_ID_FOREGROUND";
    public static String MAIN_ACTION = "com.example.routesdrawer.locationservice.action.main";
    private NotificationCompat.Builder notificationBuilder;
    private boolean isServiceRunningRightNow = false;

    private FusedLocationProviderClient fusedLocationProviderClient;

    private PowerManager.WakeLock wakeLock;
    private PowerManager powerManager;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("pttt", "onStartCommand A");

        if (intent == null) {
            Log.d("pttt", "intent == null");
            stopForeground(true);
            return START_NOT_STICKY;
        }

        String action = intent.getAction();
        if (action.equals(START_FOREGROUND_SERVICE)) {
            Log.d("pttt", "Start to recording");
            if (isServiceRunningRightNow) {
                return START_STICKY;
            }
            isServiceRunningRightNow = true;
            notifyToUserForForegroundService(intent.getClass());
            startRecording();
        } else if (action.equals(STOP_FOREGROUND_SERVICE)) {
            Log.d("pttt", "Stop recording");
            stopRecording();
        }

        return START_STICKY;
    }

    private LocationListener locationCallback = location -> {
        double lat = location.getLatitude();
        double lon = location.getLongitude();
        double altitude = location.getAltitude();
        float speed = location.getSpeed() * 3.6f;
        float bearing = location.getBearing();

        MyLoc myLoc = new MyLoc()
                .setLat(lat)
                .setLon(lon)
                .setAltitude(altitude)
                .setBearing(bearing)
                .setSpeed(speed);
        RoutesManager.getInstance(getApplicationContext()).addLocation(myLoc);
    };

    private void startRecording() {
        powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "PassiveApp:tag");
        wakeLock.acquire();

        // Run GPS
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            LocationRequest locationRequest = new LocationRequest.Builder(2000)
                    .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
                    .setMinUpdateDistanceMeters(2.0f)
                    .setMinUpdateIntervalMillis(2000)
                    .build();

            fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper());
        }

    }

    private void stopRecording() {
        Log.d("pttt", "stopRecording called");
        if (wakeLock != null && wakeLock.isHeld()) {
            wakeLock.release();
        }

        if (fusedLocationProviderClient != null) {
            fusedLocationProviderClient.removeLocationUpdates(locationCallback).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        Log.d("pttt", "stop Location Callback removed.");
                    } else {
                        Log.d("pttt", "stop Failed to remove Location Callback.");
                    }
                    RoutesManager.getInstance(getApplicationContext()).completeCurrentRoute(); // Complete current route
                    stopForeground(true);
                    stopSelf();
                    isServiceRunningRightNow = false;
                    Log.d("pttt", "Service stopped successfully");
                }
            });
        } else {
            RoutesManager.getInstance(getApplicationContext()).completeCurrentRoute(); // Complete current route
            stopForeground(true);
            stopSelf();
            isServiceRunningRightNow = false;
            Log.d("pttt", "Service stopped successfully without location updates");
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void notifyToUserForForegroundService(Class<?> targetActivityClass) {
        // On notification click
        Intent notificationIntent = new Intent(this, targetActivityClass);
        notificationIntent.setAction(MAIN_ACTION);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, NOTIFICATION_ID, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        // Use default channel name and description
        String channelName = DEFAULT_CHANNEL_NAME;
        String channelDescription = DEFAULT_CHANNEL_DESCRIPTION;

        notificationBuilder = getNotificationBuilder(this, CHANNEL_ID, NotificationManagerCompat.IMPORTANCE_LOW, channelName, channelDescription); // Low importance prevents visual appearance for this notification channel on top

        notificationBuilder
                .setContentIntent(pendingIntent) // Open activity
                .setOngoing(true)
                .setSmallIcon(R.drawable.marker_24)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher_round))
                .setContentTitle("Routes Drawer is running, click to open")
                .setContentText("Location service is running")
                .setPriority(NotificationCompat.PRIORITY_LOW);

        Notification notification = notificationBuilder.build();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(NOTIFICATION_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION);
        }

        if (NOTIFICATION_ID != lastShownNotificationId) {
            final NotificationManager notificationManager = (NotificationManager) getSystemService(Service.NOTIFICATION_SERVICE);
            notificationManager.cancel(lastShownNotificationId);
        }
        lastShownNotificationId = NOTIFICATION_ID;
    }


    public static NotificationCompat.Builder getNotificationBuilder(Context context, String channelId, int importance, String channelName, String channelDescription) {
        NotificationCompat.Builder builder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            prepareChannel(context, channelId, importance, channelName, channelDescription);
            builder = new NotificationCompat.Builder(context, channelId);
        } else {
            builder = new NotificationCompat.Builder(context);
        }
        return builder;
    }

    @TargetApi(26)
    private static void prepareChannel(Context context, String id, int importance, String channelName, String channelDescription) {
        final NotificationManager nm = (NotificationManager) context.getSystemService(Service.NOTIFICATION_SERVICE);

        if (nm != null) {
            NotificationChannel nChannel = nm.getNotificationChannel(id);

            if (nChannel == null) {
                nChannel = new NotificationChannel(id, channelName, importance);
                nChannel.setDescription(channelDescription);
                nChannel.enableLights(true);
                nChannel.setLightColor(Color.BLUE);

                nm.createNotificationChannel(nChannel);
            }
        }
    }

}
