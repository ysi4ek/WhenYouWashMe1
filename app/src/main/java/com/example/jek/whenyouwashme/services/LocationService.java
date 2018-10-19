package com.example.jek.whenyouwashme.services;

import android.Manifest;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.icu.util.Calendar;
import android.location.Location;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.PlaceLikelihood;
import com.google.android.gms.location.places.PlaceLikelihoodBuffer;
import com.google.android.gms.location.places.Places;

/**
 * Created by jek on 04.07.2017.
 */

// сервис используется для получения текущих координат пользователя и запуска
// AlarmManager'а (внутренний сервис андроида, который следит за временем грубо говоря)

public class LocationService extends Service implements
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {
    private static final String TAG = LocationService.class.getSimpleName();
    private static final long INTERVAL = 1000 * 60 * 60 * 6; // <--- 1 minute ("1000 * 60 * 60 * 24" <--- 1 day) in milliseconds
    private static final long FUTURE_TIME = 1000 * 60 * 60 * 5;
    private static final String ALARM_TIME = "time";
    private static Calendar cal = null;
    public static Location currentLocation;
    private GoogleApiClient mGoogleApiClient;
    private IBinder binder = new MapBinder();
    public static final String ACTION_LOCATION = "action location";
    public static long alarmTime;
    public static final int WEATHER_FORECAST_SERVICE_ID = 375;

    public static final String WEATHER_FORECAST_CLIENT_LOCATION = "client location";

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        alarmWeatherForecastService();
        return binder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        GoogleApiClient.Builder builder = new GoogleApiClient.Builder(this);
        mGoogleApiClient = builder.
                addConnectionCallbacks(this).
                addConnectionCallbacks(this).
                addApi(LocationServices.API).
                addApi(Places.GEO_DATA_API).
                addApi(Places.PLACE_DETECTION_API).
                //enableAutoManage(this, this).
                        build();
        mGoogleApiClient.connect();
    }


    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.d(TAG, "Location service connected");
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        currentLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (currentLocation != null) {
            Log.d(TAG, "initial location: " + currentLocation.getLatitude() + " " + currentLocation.getLongitude());
            Intent intent = new Intent(ACTION_LOCATION);
            sendBroadcast(intent);
        }
        LocationRequest locationRequest = new LocationRequest();
        locationRequest.setInterval(2 * 60 * 1000);
        locationRequest.setFastestInterval(60 * 1000);
        locationRequest.setSmallestDisplacement(1);
        locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, locationRequest, this);

        PendingResult<PlaceLikelihoodBuffer> result = Places.PlaceDetectionApi
                .getCurrentPlace(mGoogleApiClient, null);
        result.setResultCallback(new ResultCallback<PlaceLikelihoodBuffer>() {
            @Override
            public void onResult(PlaceLikelihoodBuffer likelyPlaces) {
                for (PlaceLikelihood placeLikelihood : likelyPlaces) {
                    Log.i(TAG, String.format("Place '%s' has likelihood: %g",
                            placeLikelihood.getPlace().getName(),
                            placeLikelihood.getLikelihood()));
                }
                likelyPlaces.release();
            }
        });
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.d(TAG, "Location service suspended");
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.d(TAG, "Location service failed");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand");
        return START_STICKY;
    }

    public void alarmWeatherForecastService() {
        Context ctx = getApplicationContext();
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        SharedPreferences.Editor editor = preferences.edit();
/** this gives us the time for the first trigger.  */
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            cal = Calendar.getInstance();
            Log.d(TAG, "time in millis: " + String.valueOf(cal.getTimeInMillis()));
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            if(cal.getTimeInMillis() > alarmTime){

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    editor.putLong(ALARM_TIME, (cal.getTimeInMillis() + FUTURE_TIME));//current time + 6 hours in millis
                }
                editor.apply();

                AlarmManager am = (AlarmManager) ctx.getSystemService(Context.ALARM_SERVICE);
                Intent serviceIntent = new Intent(ctx, LocationService.class);
    // make sure you **don't** use *PendingIntent.getBroadcast*, it wouldn't work
                PendingIntent servicePendingIntent =
                        PendingIntent.getService(ctx,
                                LocationService.WEATHER_FORECAST_SERVICE_ID, // integer constant used to identify the service
                                serviceIntent,
                                PendingIntent.FLAG_CANCEL_CURRENT);  // FLAG to avoid creating a second service if there's already one running
    // there are other options like setInexactRepeating, check the docs
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    am.setRepeating(
                            AlarmManager.RTC_WAKEUP,//type of alarm. This one will wake up the device when it goes off, but there are others, check the docs
                            cal.getTimeInMillis(),
                            INTERVAL,
                            servicePendingIntent
                    );
                }
                alarmTime = preferences.getLong("time", 0);//get alarm time from shared preferences
            }
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        this.currentLocation = location;
        if (currentLocation != null) {
            Log.d(TAG, location.getLatitude() + " " + location.getLongitude());
            Intent intent = new Intent(ACTION_LOCATION);
            sendBroadcast(intent);
        } else {
            Log.d(TAG, "incoming location was null!");
        }
    }

    public class MapBinder extends Binder {
        public LocationService getService() {
            return LocationService.this;
        }
    }
}