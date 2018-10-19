package com.example.jek.whenyouwashme.model.weatherForecast;

import android.content.Context;
import android.location.Location;
import android.util.Log;

import com.example.jek.whenyouwashme.R;
import com.example.jek.whenyouwashme.services.LocationService;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DecimalFormat;

public class RemoteFetch {
    private static final String TAG = RemoteFetch.class.getSimpleName();

    public JSONObject getJSON(Context context) {

        try {
            URL url = new URL(initLocation(context));
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.addRequestProperty("x-api-key", context.getString(R.string.open_weather_maps_app_id));
            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuilder json = new StringBuilder(1024);
            String tmp;
            while ((tmp = reader.readLine()) != null)
                json.append(tmp).append("\n");
            reader.close();

            JSONObject data = new JSONObject(json.toString());
            if (data.getInt("cod") != 200) {
                return null;
            }
            Log.d(TAG, "JSONobject: " + data.toString());
            return data;
        } catch (Exception e) {
            return null;
        }
    }

    private String initLocation(Context context) {
        Location location = LocationService.currentLocation;
        double latitude = location.getLatitude();
        DecimalFormat df = new DecimalFormat("###.####");
        double longitude = location.getLongitude();
        String latitudeFormatted = df.format(latitude).replace(",", ".");
        String longitudeFormatted = df.format(longitude).replace(",", ".");
        StringBuilder sbWeatherForecastURL = new StringBuilder("http://api.openweathermap.org/data/2.5/forecast?units=metric&");
        sbWeatherForecastURL
                .append("lat=")
                .append(latitudeFormatted)
                .append("&lon=")
                .append(longitudeFormatted)
                .append("&appid=")
                .append(context.getString(R.string.open_weather_maps_app_id));

        Log.d(TAG, "get URL: " + sbWeatherForecastURL);
        return sbWeatherForecastURL.toString();
    }
}
