# **RoutesDrawerLibrary**

`RoutesDrawerLibrary` is an Android library designed to help developers easily track, visualize, and manage location-based routes on a Google Map. It provides tools to draw polylines that dynamically change color based on speed, manage location data, and take snapshots of the map.

## **Features**

- **Polyline Visualization**: Color-changing polylines based on route speed.
- **Location Management**: Stores latitude, longitude, altitude, speed, and bearing.
- **Map Snapshot**: Captures the current map view as a bitmap.
- **Automatic Permissions**: Requests necessary permissions automatically.
- **Encrypted Shared Preferences**: Securely stores sensitive data using encrypted shared preferences, ensuring that your app's data remains protected and confidential. 

## **Installation**

1. **Add the Dependency**
    
    To use `RoutesDrawerLibrary`, add the following dependency to your `build.gradle` file:
    ```gradle
    dependencies {  
    implementation 'com.google.android.gms:play-services-maps:18.0.2'
    } 
    ```
    
2. **Permissions**
    
    The permissions are specified at the manifest file in the library
    
```gradle
 <uses-permission android:name="android.permission.POST_NOTIFICATIONS" />
    <uses-permission android:name="android.permission.FOREGROUND_SERVICE" />
    <uses-permission android:name="android.permission.FOREGROUND_SERVICE_LOCATION" />
    <uses-permission android:name="android.permission.WAKE_LOCK" />
    <uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
    <uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
    
```
3. Create GoogleMap fragment or MapView
## **Usage**
### 1.  Initializing RoutesManager

Create an instance of RoutesManager `RoutesManager.getInstance(context)`
### **2.  Start  service**
To start recording data you need to start the service from the RoutesManager from the function 
`startServiceIntent(context)` .
It will automatically check and request the needed permissions .
### 3.  Save route
When stopping the service the current recorded route will be saved and stored into the shared preference 
### **4.  Drawing the Polyline on the Map**

To draw the route on a `GoogleMap`:
select the desired route from the routes list in the RoutesManager

```java
GoogleMap map; // Initialize your GoogleMap route.addPolyline(map);
```
### **3. Taking a Map Snapshot**

You can capture a snapshot of the map with the drawn route:
```java
route.getBitmap(map, new Route.OnBitmapReadyCallback() {     @Override     public void onBitmapReady(Bitmap bitmap) {  
// Use the bitmap (e.g., save it or display it)  
routesManager.saveImage(bitmap, route.getName()) // To save the bitmap to photos
	} 													   
});
```

## Key Methods :

- **`addPolyline(GoogleMap map)`**: Draws polylines on a Google Map, with colors that change according to the speed of the route's segments.
- **`getBitmap(GoogleMap map, OnBitmapReadyCallback callback)`**: Captures a snapshot of the current map view as a bitmap and provides it through a callback.
- **`saveImage(Bitmap finalBitmap, String imageName)`**: Saves a bitmap image to the device's storage with the specified name.
- **`startServiceIntent(Context context)`**: Starts the location tracking service with the necessary context.
- **`stopServiceIntent(Context context)`**: Stops the location tracking service using the provided context.
# ScreenShots
<img src="https://github.com/user-attachments/assets/d35d131b-6162-463e-8f69-f4d2862f2c4e" alt="Screenshot" height="700"/>
<img src="https://github.com/user-attachments/assets/b389e8b8-fcef-465c-becc-f24145290577" alt="Screenshot" height="700"/>


