package com.cabralesandresltda.test;

import android.Manifest;
import android.app.Activity;
import android.app.Application;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class PluginInstance extends Application {

    public static Activity unityActivity;
    static double latitude;
    static double longitude;
    static float speed;
    static Context appContext;
    Date currentDate;
    LocationService mServer;
    static final String DISTANCE="distance";
    static final String TIME="time";
    static final String RUNNING="running";
    static final String SPEED="speed";
    static final String LATLONG="latlong";
    static final String LOCATIONS="locations";
    static final String ENDDISTANCE="enddistance";
    static final String ENDTIME="endtime";
    static final String AVGSPEED="avgspeed";
    static final String DISTANCETO="todistance";




    public static void ReceiveActivityInstance(Activity tActivity){
        unityActivity = tActivity;

        String[] perms = new String[1];
        /*
        perms[0] = Manifest.permission.ACCESS_BACKGROUND_LOCATION;
        if(ContextCompat.checkSelfPermission(unityActivity, Manifest.permission.ACCESS_BACKGROUND_LOCATION)
                != PackageManager.PERMISSION_GRANTED){
            Log.i("LOCATION", "Permission isn't granted");
            ActivityCompat.requestPermissions(unityActivity,
                    new String[]{Manifest.permission.ACCESS_COARSE_LOCATION,
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_BACKGROUND_LOCATION
                    }, PackageManager.PERMISSION_GRANTED);
        }
        */
        perms[0] = Manifest.permission.ACCESS_FINE_LOCATION;
        if(ContextCompat.checkSelfPermission(unityActivity, Manifest.permission.ACCESS_FINE_LOCATION)
        != PackageManager.PERMISSION_GRANTED){
            Log.i("LOCATION", "Permission isn't granted");
            ActivityCompat.requestPermissions(PluginInstance.unityActivity, perms, 1);
        }
        perms[0] = Manifest.permission.ACCESS_BACKGROUND_LOCATION;
        if(ContextCompat.checkSelfPermission(unityActivity, Manifest.permission.ACCESS_BACKGROUND_LOCATION)
                != PackageManager.PERMISSION_GRANTED){
            Log.i("LOCATION", "Permission isn't granted");
            ActivityCompat.requestPermissions(PluginInstance.unityActivity, perms, 1);
        }
    }
    public String Toast(String msg){
        String[] perms = new String[1];
        perms[0] = Manifest.permission.ACCESS_BACKGROUND_LOCATION;
        ActivityCompat.requestPermissions(PluginInstance.unityActivity, perms, 1);
        return " " + (ContextCompat.checkSelfPermission(unityActivity, Manifest.permission.ACCESS_BACKGROUND_LOCATION)
                != PackageManager.PERMISSION_GRANTED);
    }
    public static void StartService(){
        if(unityActivity != null){

            start();
        }
    }
    public static boolean hasBackground(){
        if(ContextCompat.checkSelfPermission(unityActivity, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED){
            return false;
        }else{
            if(ContextCompat.checkSelfPermission(unityActivity, Manifest.permission.ACCESS_BACKGROUND_LOCATION)
                    != PackageManager.PERMISSION_GRANTED){
                return false;
            }
        }
        return true;
    }
    public static void requestPermission(){
        String[] perms = new String[1];
        perms[0] = Manifest.permission.ACCESS_FINE_LOCATION;
        if(ContextCompat.checkSelfPermission(unityActivity, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED){
            Log.i("LOCATION", "Permission isn't granted");
            ActivityCompat.requestPermissions(PluginInstance.unityActivity, perms, 1);
        }else{
            perms[0] = Manifest.permission.ACCESS_BACKGROUND_LOCATION;
            if(ContextCompat.checkSelfPermission(unityActivity, Manifest.permission.ACCESS_BACKGROUND_LOCATION)
                    != PackageManager.PERMISSION_GRANTED){
                Log.i("LOCATION", "Permission isn't granted");
                ActivityCompat.requestPermissions(PluginInstance.unityActivity, perms, 1);
            }
        }
    }
    private static void start(){
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(appContext);
        SharedPreferences.Editor editor = sharedPreferences.edit();;
        editor.clear();
        editor.putString(TIME, "00:00");
        editor.apply();
        unityActivity.startForegroundService(new Intent(unityActivity, LocationService.class));
        StartRun();
    }
    public static void StopService(){
        Intent serviceIntent = new Intent(unityActivity, LocationService.class);
        unityActivity.stopService(serviceIntent);
        //StopRun();
        //FinishRun();
    }
    public static String getLatitude(){
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(appContext);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        String latitude = sharedPreferences.getString(LATLONG, "DEFAULT");
        return latitude;
    }
    public static float getSpeed(){
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(appContext);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        return sharedPreferences.getFloat(SPEED, 0f);
    }
    public static float getDistance(){
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(appContext);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        return sharedPreferences.getFloat(DISTANCE, 0f);
    }
    public static int getTrueTime(){
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(appContext);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        return sharedPreferences.getInt(ENDTIME, 0);
    }

    public static String getTime(){
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(appContext);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        return sharedPreferences.getString(TIME, "00:00");
    }
    public static String getDistanceto(){
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(appContext);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        return sharedPreferences.getString(DISTANCETO, "0");
    }
    public static String getAllSpots(){
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(appContext);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        return sharedPreferences.getString(LATLONG, "DEFAULT");
    }
    public static String getCurrentPosition(){
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(appContext);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        String latlong = "corsa";
        if(sharedPreferences.getBoolean(RUNNING, false)){
            latlong = sharedPreferences.getString(LATLONG, "DEFAULT");
        }
        else{
            latlong = "Bola";
        }
        return latlong;
    }
    public static void StartRun(){
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(appContext);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(RUNNING, true);
        editor.apply();
        Log.i("LOCATION", "NOW RUNNING");
    }
    public static void StopRun(){
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(appContext);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(RUNNING, false);
        editor.apply();
        Log.i("LOCATION", "RUN STOPPED");
    }
    public static String SyncPositions(){
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(appContext);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        Set<String> set = sharedPreferences.getStringSet(LOCATIONS, new HashSet<>());
        List<String> points = new ArrayList<String>();
        for (String x : set)
            points.add(x);
        return points.toString();
    }
    public static void FinishRun(){
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(appContext);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        PluginInstance.appContext=getApplicationContext();
    }
}
