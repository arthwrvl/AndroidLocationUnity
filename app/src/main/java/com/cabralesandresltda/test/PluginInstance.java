package com.cabralesandresltda.test;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.preference.PreferenceManager;
import android.util.Log;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.Date;

public final class PluginInstance extends Application {

    public static PluginInstance SINGLETON;
    public static Activity unityActivity;
    public static Intent intent;
    static Context appContext;
    static final String DISTANCE="distance";
    static final String TIME="time";
    static final String RUNNING="running";
    static final String SPEED="speed";
    static final String CURRENTLOCATION="currentlocation";
    static final String SPOTLIST="spotlist";
    static final String ENDDISTANCE="enddistance";
    static final String ENDTIME="endtime";
    static final String CURRENTRYTHM="currentrythm";
    static final String LISTRYTHM="listrythm";
    static final String MAXSPEED="maxspeed";
    static final String MINSPEED="minspeed";
    static final String AVGSPEED="avgspeed";
    static final String DISTANCETO="todistance";



    public static void ReceiveActivityInstance(Activity tActivity){
        unityActivity = tActivity;

        String[] perms = new String[1];

        requestPermission();
    }
    public PluginInstance getInstance(){
        if(SINGLETON == null){
            synchronized (PluginInstance.class){
                if(SINGLETON == null){
                    SINGLETON = this;
                }
            }
        }
        return SINGLETON;
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
        AlertDialog.Builder builder = new AlertDialog.Builder(PluginInstance.unityActivity);

        builder.setMessage("Este aplicativo precisa usar a localização o tempo todo para registrar o percurso das atividades")
                .setTitle("Conceda Acesso à Localização");
        AlertDialog dialog = builder.create();
        String[] perms = new String[1];
        perms[0] = Manifest.permission.ACCESS_FINE_LOCATION;
        if(ContextCompat.checkSelfPermission(unityActivity, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED){
            dialog.show();
            Log.i("LOCATION", "Permission isn't granted");
            ActivityCompat.requestPermissions(PluginInstance.unityActivity, perms, 1);
        }else{
            perms[0] = Manifest.permission.ACCESS_BACKGROUND_LOCATION;
            if(ContextCompat.checkSelfPermission(unityActivity, Manifest.permission.ACCESS_BACKGROUND_LOCATION)
                    != PackageManager.PERMISSION_GRANTED){
                dialog.show();
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
        intent = new Intent(unityActivity, LocationService.class);
        unityActivity.startForegroundService(intent);
        StartRun();
    }
    public static void StopService(){
        Intent serviceIntent = new Intent(unityActivity, LocationService.class);
        Log.d("FINISHING IT", unityActivity.stopService(intent) + " " + intent.getDataString());
        StopRun();
        //FinishRun();
    }
    public static String getLatitude(){
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(appContext);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        String latitude = sharedPreferences.getString(CURRENTLOCATION, "DEFAULT");
        return latitude;
    }
    public static float getSpeed(){
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(appContext);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        return sharedPreferences.getFloat(SPEED, 0f);
    }
    public static float getRythm(){
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(appContext);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        return sharedPreferences.getFloat(CURRENTRYTHM, 0f);
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
        return sharedPreferences.getString(SPOTLIST, "DEFAULT");
    }    
    public static String getAllPaces(){
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(appContext);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        return sharedPreferences.getString(LISTRYTHM, "DEFAULT");
    }
    public static String getCurrentPosition(){
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(appContext);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        String latlong = "corsa";
        if(sharedPreferences.getBoolean(RUNNING, false)){
            latlong = sharedPreferences.getString(CURRENTLOCATION, "DEFAULT");
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
