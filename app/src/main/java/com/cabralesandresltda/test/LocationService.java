package com.cabralesandresltda.test;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;

import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;

import android.os.Build;

import android.os.CancellationSignal;
import android.os.Handler;
import android.os.HandlerThread;

import android.os.Vibrator;
import android.os.IBinder;

import android.os.Looper;

import android.preference.PreferenceManager;
import android.util.Log;


import androidx.annotation.Nullable;

import androidx.core.app.ActivityCompat;

import androidx.core.app.NotificationManagerCompat;
import java.util.ArrayList;
import java.util.List;

public class LocationService extends Service {

    HandlerThread thread = new HandlerThread("TimerThread");
    private final Handler handler = new Handler();
    private final Handler handler2 = new Handler();
    private Context context;
    public SharedPreferences sharedPreferences;
    LocationManager locationManager;
    float speed;
    float maxspeed = 0;
    float minspeed = 100;
    float distance;
    float currentRythm = 0;
    float currentKMRythm = 0;
    public float minDistance = 10;
    public float maxDistance = 100;
    int wait = 10;
    float lastRecordedTime = 0;
    Location lastKnownLocation = null;
    int exactLapTime;
    int timeStarted;
    private int lapTime;
    private float lastKMTime = 0;
    private Vibrator vibrator;
    ArrayList<Location> positions = new ArrayList<Location>();
    ArrayList<Float> KMReachTime = new ArrayList<Float>();
    String allSpots = "";
    float singleDistance = 0;
    String allPaces = "";


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
        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        positions = new ArrayList<>();
        allSpots = "";
        distance = 0;
        createNotificationChannel();
        startNotification();
        timeStarted = (int) System.currentTimeMillis() / 100;
        handler2.removeCallbacks(UpdateLocation);
        handler2.postDelayed(UpdateLocation, 1000);
        return START_NOT_STICKY;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        context = this;
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
    }

    private Runnable UpdateLocation = new Runnable() {
        @Override
        public void run() {
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            SharedPreferences.Editor editor = sharedPreferences.edit();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                locationManager.getCurrentLocation(LocationManager.GPS_PROVIDER, new CancellationSignal(), context.getMainExecutor(), location -> lastKnownLocation = location);
            }else{
                locationManager.requestSingleUpdate(LocationManager.GPS_PROVIDER, location -> lastKnownLocation = location, Looper.myLooper());
            }
            if(wait <= 0){
                lapTime = ((int) System.currentTimeMillis() / 1000) - (timeStarted / 10) - 10;
                editor.putString(PluginInstance.TIME, formatClock(lapTime));
                editor.putInt(PluginInstance.ENDTIME, lapTime);
                if(lastKnownLocation != null){
                    if(positions.size() > 0){
                        Location current = positions.get(positions.size() - 1);
                        float dist = current.distanceTo(lastKnownLocation);
                        if(dist >= minDistance){
                            positions.add(lastKnownLocation);
                            exactLapTime = ((int) System.currentTimeMillis() / 100) - timeStarted - 10;
                            float rawPace = ((float) (exactLapTime - lastRecordedTime) / 600) / (dist / 1000);
                            float remaining = rawPace % 1;
                            currentRythm = (rawPace - remaining) + (remaining * 0.6f);
                            positions.add(lastKnownLocation);
                            distance += dist;
                            speed = lastKnownLocation.getSpeed() * 3.6f;
                            String spot = lastKnownLocation.getLatitude() + "#" + lastKnownLocation.getLongitude() + "#" + lastKnownLocation.getAccuracy() + "|";
                            if(positions.size() % 3 == 0){
                                allSpots += spot;
                            }
                            editor.putString(PluginInstance.CURRENTLOCATION, spot);
                            editor.putString(PluginInstance.SPOTLIST, allSpots);
                            editor.putFloat(PluginInstance.DISTANCE, distance);
                            editor.putFloat(PluginInstance.SPEED, speed);
                            editor.putFloat(PluginInstance.CURRENTRYTHM, currentRythm);
                            lastRecordedTime = exactLapTime;
                        }
                    }else{
                        positions.add(lastKnownLocation);
                        editor.putString(PluginInstance.CURRENTLOCATION, lastKnownLocation.getLatitude() + "#" + lastKnownLocation.getLongitude() + "#" + lastKnownLocation.getAccuracy() + "|");
                    }
                }
            }else{
                wait--;
                editor.putString(PluginInstance.TIME, wait + "");
            }
            editor.apply();
            handler2.postDelayed(this, 1000);
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

    @Override
    public void onDestroy() {
        String pacesAsText = "";
        SharedPreferences.Editor editor = sharedPreferences.edit();
        for(int i = 0; i < KMReachTime.size(); i++){
            pacesAsText += KMReachTime.get(i).toString() + "#";
        }
        pacesAsText += lapTime - lastKMTime + "#";

        editor.putString(PluginInstance.LISTRYTHM, pacesAsText);
        editor.apply();
        handler2.removeCallbacks(UpdateLocation);
        stopForeground(true);
        stopSelf();
        thread.interrupt();
        super.onDestroy();
    }
}
