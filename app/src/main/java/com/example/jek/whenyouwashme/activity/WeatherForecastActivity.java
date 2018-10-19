package com.example.jek.whenyouwashme.activity;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.example.jek.whenyouwashme.R;
import com.example.jek.whenyouwashme.fragments.FragmentLowerPart;
import com.example.jek.whenyouwashme.fragments.FragmentRightPart;
import com.example.jek.whenyouwashme.fragments.FragmentWeather;
import com.example.jek.whenyouwashme.model.weatherForecast.RemoteFetch;
import com.example.jek.whenyouwashme.services.LocationService;

import org.json.JSONObject;

// активити отрисовывающее погоду

public class WeatherForecastActivity extends AppCompatActivity {
    private static final String TAG = WeatherForecastActivity.class.getSimpleName();
    LocationService locationService;
    RemoteFetch remoteFetch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate: ");
//        Log.d(TAG, "onCreate: ");
//        FragmentManager fragmentManager = getFragmentManager();
//        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
//        FragmentWeather fragmentWeather = new FragmentWeather();
//        FragmentLowerPart fragmentLowerPart = new FragmentLowerPart();
//        FragmentRightPart fragmentRightPart = new FragmentRightPart();
//
//          первым if'ом определяем ориентацию экрана - портретный/ландшафтный
//          второй if перерисовывает фрагменты активити, удаляя старые, иначе они накладываются друг на друга
//          после каждого поворота экрана
        if (getResources().getConfiguration().orientation ==
                Configuration.ORIENTATION_PORTRAIT) {
            if (savedInstanceState == null) {
                setContentView(R.layout.activity_main_portrait);
                Log.d(TAG, "savedInstanceState == null, portrait orient");
                getFragmentManager().beginTransaction().replace(R.id.weather_part, FragmentWeather.newInstance()).commit();
                getFragmentManager().beginTransaction().replace(R.id.lower_part, FragmentLowerPart.newInstance()).commit();
            } else {
                Log.d(TAG, "savedInstanceState != null, portrait orient");
                setContentView(R.layout.activity_main_portrait);
            }

//            fragmentTransaction.add(R.id.weather_part, fragmentWeather);
//            fragmentTransaction.add(R.id.lower_part, fragmentLowerPart);
//            fragmentTransaction.commit();
        } else {
            if (savedInstanceState == null) {
                Log.d(TAG, "savedInstanceState == null, landscape orient");
                setContentView(R.layout.activity_main_landscape);
                getFragmentManager().beginTransaction().replace(R.id.weather_part, FragmentWeather.newInstance()).commit();
                getFragmentManager().beginTransaction().replace(R.id.right_part, FragmentRightPart.newInstance()).commit();
            } else {
                Log.d(TAG, "savedInstanceState != null, landscape orient");
                setContentView(R.layout.activity_main_landscape);
                getFragmentManager().beginTransaction().replace(R.id.right_part, FragmentRightPart.newInstance()).commit();
                //Log.d(TAG, "switch to else");
            }
//            fragmentTransaction.add(R.id.weather_part, fragmentWeather);
//            fragmentTransaction.add(R.id.right_part, fragmentRightPart);
//            fragmentTransaction.commit();
        }

        remoteFetch = new RemoteFetch();

    }

    //в методе биндимся к сервису LocationService
    //и регистрируем наш ресивер на получение данных от сервиса
    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    //отключаемся от сервиса и отменяем регистрацию ресивера
    @Override
    protected void onStop() {
        super.onStop();
        unbindService(connection);
    }

    private ServiceConnection connection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {

            WeatherForecastActivity.this.locationService = ((LocationService.MapBinder) service).getService();
            Log.d(TAG, "service connected " + System.currentTimeMillis());
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.d(TAG, "service disconnected");
            WeatherForecastActivity.this.locationService = null;
        }
    };

    public JSONObject fetchWeather(){
        return remoteFetch.getJSON(this);
    }

}