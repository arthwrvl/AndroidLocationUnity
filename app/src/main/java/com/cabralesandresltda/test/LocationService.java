package com.cabralesandresltda.test;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.Icon;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Binder;
import android.os.Looper;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

public class LocationService extends Service {

    HandlerThread thread = new HandlerThread("TimerThread");
    private final Handler handler = new Handler();

    public SharedPreferences sharedPreferences;
    LocationManager locationManager;
    boolean valid;
    double latitude;
    double longitude;
    float speed;
    float distance;
    float currentRythm = 0;
    float currentKMRythm = 0;
    public float minDistance = 10;
    public float maxDistance = 100;
    int wait = 10;
    int lastRecordedTime = 0;
    Location lastKnownLocation = null;
    int timeStarted;
    private int lapTime;
    private int lastKMTime = 0;
    public int updates = 0;
    ArrayList<Location> positions = new ArrayList<Location>();
    ArrayList<Float> paces = new ArrayList<Float>();
    ArrayList<Location> fullPositions = new ArrayList<Location>();
    String allSpots = "";
    float singleDistance = 0;
    String allPaces = "";

    Set<String> trackedLocations = new HashSet<String>();


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    //create Notification Channel
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            final NotificationChannel notificationChannel = new NotificationChannel(
                    "LocationLib",
                    "ServiceChannel",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            final NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(this);
            notificationManagerCompat.createNotificationChannel(notificationChannel);
        }
    }

    //start the notification
    private void startNotification() {
        String input = "Registrando sua localização";

        //intent for unity screen
        Intent notificationIntent = new Intent(this, PluginInstance.unityActivity.getClass());
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE);

        //create notification
        Notification notification = new Notification.Builder(this, "LocationLib")
                .setContentText(input)
                .setContentTitle("GPS em uso")
                .setSmallIcon(R.drawable.ic_icon)
                .setContentIntent(pendingIntent)
                .setColor(Color.argb(1, 0.2f, 0.5f, 0.98f))
                .setWhen(System.currentTimeMillis())
                .build();
        startForeground(112, notification);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        createNotificationChannel();
        startNotification();
        requestLocation();
        timeStarted = (int)System.currentTimeMillis() / 1000;
        handler.removeCallbacks(Update);
        handler.postDelayed(Update, 1000);
        return START_NOT_STICKY;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
    }


    private Runnable Update = new Runnable() {
        @Override
        public void run() {

            SharedPreferences.Editor editor = sharedPreferences.edit();
            if(wait <= 0){
                updates++;
                lapTime = ((int) System.currentTimeMillis() / 1000) - timeStarted - 10;
                editor.putString(PluginInstance.TIME, formatClock(lapTime));
                editor.putFloat(PluginInstance.DISTANCE, distance);
                editor.putFloat(PluginInstance.SPEED, speed);
                editor.putFloat(PluginInstance.CURRENTRYTHM, currentRythm);
                editor.putInt(PluginInstance.ENDTIME, lapTime);
                if(fullPositions.size() > 0){
                    allSpots += lastKnownLocation.getLatitude()+"#"+lastKnownLocation.getLongitude()+"#"+lastKnownLocation.getAccuracy()+"|";
                    editor.putString(PluginInstance.LATLONG, lastKnownLocation.getLatitude()+"#"+lastKnownLocation.getLongitude()+"#"+lastKnownLocation.getAccuracy()+"|");
                    editor.putString(PluginInstance.LOCATIONS, allSpots);
                }
                editor.apply();
                Log.d("AHAHAHA", "run: " + formatClock(lapTime));
                Log.d("AHAHAHA", "run: " + distance);
                Log.d("AHAHAHA", "run: " + speed);


            }else{
                wait--;
                editor.putString(PluginInstance.TIME, wait + "");
                editor.apply();
                Log.d("AHAHAHA", "run: " + wait);
            }
            handler.postDelayed(this, 1000);
        }
    };

    private String formatClock(int seconds){
        String clock;
        int hours = 0;
        int minutes = 0;
        hours = seconds/3600;
        minutes = (seconds % 3600) / 60;
        seconds = seconds % 60;
        if(hours > 0){
            clock = String.format("%02d:%02d:%02d", hours, minutes, seconds);
        }else{
            clock = String.format("%02d:%02d", minutes, seconds);
        }
        return clock;
    }

    public void SyncPositions() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putStringSet(PluginInstance.LOCATIONS, trackedLocations);
    }

    private void requestLocation() {
        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    Activity#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for Activity#requestPermissions for more details.
            return;
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 10, 1, new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                SharedPreferences.Editor editor = sharedPreferences.edit();
                if(lastKnownLocation == null){
                    lastKnownLocation = location;
                }
                if(wait <= 0){
                    if(fullPositions.size() > 0){
                        Location current = positions.get(positions.size() - 1);
                        float dist = current.distanceTo(location);
                        if(dist > 2 && dist < 25){
                            fullPositions.add(location);
                        }
                    }else{
                        fullPositions.add(location);
                    }
                    if(maxDistance != 0 && minDistance != 0){
                        if(positions.size() > 0){

                            Location current = positions.get(positions.size() - 1);
                            float dist = current.distanceTo(location);
                            editor.putString(PluginInstance.DISTANCETO, dist + " " + positions.size() + " " + updates);
                            editor.apply();
                                if(dist > minDistance){
                                    lastKnownLocation = location;
                                    valid = true;
                                }
                        }else{
                            lastKnownLocation = location;
                            valid = true;
                        }
                    }else{
                        lastKnownLocation = location;
                        valid = true;
                    }
                    if(lastKnownLocation != null){
                        if(positions.size() > 0){
                            Location current = positions.get(positions.size() - 1);
                            if(valid){
                                singleDistance = current.distanceTo(lastKnownLocation);
                                float rawPace = ((float)(lapTime - lastRecordedTime)/60)/(singleDistance/1000);
                                float remaining = rawPace%1;
                                currentRythm = (rawPace - remaining) + (remaining * 0.6f);
                                Log.d("Rythm", "time" + (lapTime-lastRecordedTime) + " distance: "+ singleDistance + " Rythm: " + currentRythm + " res: " + remaining + " raw" + rawPace);
                                Log.d("Rythm", "distance in KM " + singleDistance/1000 + " time in minutes: " + (float)(lapTime - lastRecordedTime)/60);
                                speed = lastKnownLocation.getSpeed() * 3.6f;
                                distance += singleDistance;
                                currentKMRythm = ((float)((lastKMTime - lapTime)/60))/(distance/1000);
                                float rem = currentKMRythm%1;
                                currentKMRythm = (currentKMRythm - rem) + (rem * 0.6f);
                                if(distance > (paces.size() + 1) * 1000){
                                    currentKMRythm = ((float)((lastKMTime - lapTime)/60))/1;
                                    rem = currentKMRythm%1;
                                    currentKMRythm = (currentKMRythm - rem) + (rem * 0.6f);
                                    paces.add(currentKMRythm);
                                    lastKMTime = lapTime;
                                }
                                positions.add(lastKnownLocation);
                                lastRecordedTime = lapTime;
                                valid = false;
                            }
                        }else{
                            positions.add(lastKnownLocation);
                        }
                    }
                }
            }
        });
    }

    @Override
    public void onDestroy() {
        String pacesAsText = "";
        paces.add(currentKMRythm);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        for(int i = 0; i < paces.size(); i++){
            pacesAsText += paces.get(i).toString() + "#";
        }
        editor.putString(PluginInstance.LISTRYTHM, pacesAsText);
        editor.apply();
        Log.d("DESTROYING", "onDestroy: destroying IT");
        stopForeground(true);
        stopSelf();
        handler.removeCallbacks(Update);
        thread.interrupt();
        super.onDestroy();
    }
}
