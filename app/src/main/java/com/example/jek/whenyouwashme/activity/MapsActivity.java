package com.example.jek.whenyouwashme.activity;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.example.jek.whenyouwashme.R;
import com.example.jek.whenyouwashme.model.googleMaps.DataTransfer;
import com.example.jek.whenyouwashme.model.googleMaps.GetNearbyPlacesData;
import com.example.jek.whenyouwashme.services.LocationService;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;

public class MapsActivity extends AppCompatActivity
        implements OnMapReadyCallback {
    private static final String TAG = MapsActivity.class.getSimpleName();
    private static final int REQUEST_MAPS_PERMISSIONS_ON_START = 5005;
    private static final int REQUEST_MAPS_PERMISSIONS_ON_MAP_SETUP = 5006;
    private static final int PROXIMITY_RADIUS = 10000;
    private String url;
    private long serviceBindTime = System.currentTimeMillis();
    private GoogleMap googleMap;
    private LocationService service;
    private MyBroadcastReceiver myBroadcastReceiver;
    private double distance;
    private Location setCurrentLocation;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        if (!CheckGooglePlayServices()) {
            Log.d(TAG, "onCreate, Finishing test case since Google Play Services are not available");
            finish();
        } else {
            Log.d(TAG, "onCreate, Google Play Services available.");
        }
        ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map)).getMapAsync(this);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            checkPermissions(REQUEST_MAPS_PERMISSIONS_ON_START);
        } else {
            bindLocationService();
        }
        myBroadcastReceiver = new MyBroadcastReceiver();
    }

    private boolean CheckGooglePlayServices() {
        GoogleApiAvailability googleAPI = GoogleApiAvailability.getInstance();
        int result = googleAPI.isGooglePlayServicesAvailable(this);
        if (result != ConnectionResult.SUCCESS) {
            if (googleAPI.isUserResolvableError(result)) {
                googleAPI.getErrorDialog(this, result,
                        0).show();
            }
            return false;
        }
        return true;
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "onStart");
        Intent intent = new Intent(this, LocationService.class);
        bindService(intent, connection, BIND_AUTO_CREATE);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(LocationService.ACTION_LOCATION);
        registerReceiver(myBroadcastReceiver, intentFilter);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onStop() {
        super.onStop();
        unbindService(connection);
        unregisterReceiver(myBroadcastReceiver);
    }

    private void bindLocationService() {
        bindService(new Intent(this, LocationService.class), connection, BIND_AUTO_CREATE);
    }

    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.d(TAG, "service connected " + System.currentTimeMillis());

            MapsActivity.this.service = ((LocationService.MapBinder) service).getService();
            if (MapsActivity.this.service.currentLocation == null) {
                Log.d(TAG, "location is null");
            } else {
                Log.d(TAG, MapsActivity.this.service.currentLocation.toString());
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.d(TAG, "service disconnected");
            MapsActivity.this.service = null;
        }
    };

    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.d(TAG, "map is ready");
        this.googleMap = googleMap;
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            checkPermissions(REQUEST_MAPS_PERMISSIONS_ON_MAP_SETUP);
            return;
        } else {
            setupMap();
        }
    }

    private void setupMap() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        googleMap.setMyLocationEnabled(true);
    }

    private void checkPermissions(int code) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            bindLocationService();
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION}, code);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_MAPS_PERMISSIONS_ON_START && grantResults.length > 0) {

            if (grantResults[0] == PackageManager.PERMISSION_GRANTED &&
                    grantResults[1] == PackageManager.PERMISSION_GRANTED) {

                bindLocationService();

            } else {
                new AlertDialog.Builder(this).setMessage(R.string.alert_no_gps_permission).
                        setNeutralButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                finish();
                            }
                        }).show();

            }
        } else if (requestCode == REQUEST_MAPS_PERMISSIONS_ON_MAP_SETUP && grantResults.length > 0) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED &&
                    grantResults[1] == PackageManager.PERMISSION_GRANTED) {

                setupMap();

            } else {
                new AlertDialog.Builder(this).setMessage(R.string.alert_no_gps_permission).
                        setNeutralButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                finish();
                            }
                        }).show();
            }
        }
    }


    private class MyBroadcastReceiver extends BroadcastReceiver {
        Location location;
        LatLng myPosition;
        CameraUpdate center;
        CameraUpdate zoom;

        @Override
        public void onReceive(Context context, Intent intent) {
            location = MapsActivity.this.service.currentLocation;
            DataTransfer dataTransfer;
            //Object[] dataTransfer = new Object[2];
            String search = "car_wash";
            double latitude = location.getLatitude();
            double longitude = location.getLongitude();
            myPosition = new LatLng(location.getLatitude(),
                    location.getLongitude());
            Log.d(TAG, myPosition.toString());
            center = CameraUpdateFactory.newLatLng(myPosition);
            zoom = CameraUpdateFactory.zoomTo(15);
            /*googleMap.moveCamera(center);
            googleMap.animateCamera(zoom);*/
            GetNearbyPlacesData getNearbyPlacesData = new GetNearbyPlacesData();

            if (setCurrentLocation == null) {
                setCurrentLocation = location;
                String url = getUrl(latitude, longitude, search);
                dataTransfer = new DataTransfer(googleMap, url);
                //dataTransfer[0] = googleMap;
                //dataTransfer[1] = url;
                //dataTransfer = new DataTransfer(googleMap, url);
                Log.d(TAG, "dataTransfer: " + dataTransfer + " " + "googleMap: " + googleMap);
                getNearbyPlacesData.execute(dataTransfer);
                Toast.makeText(MapsActivity.this, "These are your Nearest Carwash! ",
                        Toast.LENGTH_LONG).show();
                //center map on current user location with radius 11 km
                googleMap.moveCamera(center);
                googleMap.animateCamera(CameraUpdateFactory.zoomTo(11));
            } else if (setCurrentLocation.distanceTo(location) > 5000) {
                setCurrentLocation = location;
                String url = getUrl(latitude, longitude, search);
                dataTransfer = new DataTransfer(googleMap, url);
                //dataTransfer[0] = googleMap;
                //dataTransfer[1] = url;
                getNearbyPlacesData.execute(dataTransfer);
                //center map on current user location with radius 11 km
                googleMap.moveCamera(center);
                googleMap.animateCamera(CameraUpdateFactory.zoomTo(11));
            }
        }

        private String getUrl(double latitude, double longitude, String nearbyPlace) {
            StringBuilder googlePlacesUrl = new
                    StringBuilder("https://maps.googleapis.com/maps/api/place/nearbysearch/json?");
            googlePlacesUrl.append("location=" + latitude + "," + longitude);
            googlePlacesUrl.append("&radius=" + PROXIMITY_RADIUS);
            googlePlacesUrl.append("&type=" + nearbyPlace);
            googlePlacesUrl.append("&sensor=true");
            googlePlacesUrl.append("&key=" + "AIzaSyATuUiZUkEc_UgHuqsBJa1oqaODI-3mLs0");
            return (googlePlacesUrl.toString());
        }
    }
}