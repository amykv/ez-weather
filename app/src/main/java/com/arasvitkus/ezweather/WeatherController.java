package com.arasvitkus.ezweather;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONObject;

import java.util.Set;

import cz.msebera.android.httpclient.Header;

public class WeatherController extends AppCompatActivity {

    //Constants:
    final int NEW_CITY_CODE =456;
    final String LOGCAT_TAG = "EZWeather";
    final int REQUEST_CODE = 123;
    final String WEATHER_URL = "http://api.openweathermap.org/data/2.5/weather";
    //App ID to use OpenWeather data, reikia but ah, de, be raides pirmos tris
    final String APP_ID = "btr778109372c4d12f0b391170d49efc";
    //Time between location updates (5000 milliseconds or 5 seconds)
    final long MIN_TIME = 5000;
    //Distance between location updates (1000m or 1km)
    final float MIN_DISTANCE = 1000;

    //Set LOCATION_PROVIDER for cellular network towers
    String LOCATION_PROVIDER = LocationManager.NETWORK_PROVIDER;


    //Member variables:
    TextView mCityLabel;
    ImageView mWeatherImage;
    TextView mTemperatureLabel;

    //LocationManager and a LocationListener declared
    LocationManager mLocationManager;
    LocationListener mLocationListener;
    private boolean mUseLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.weather_controller_layout);

        // Linking the elements in the layout to Java code
        mCityLabel = (TextView) findViewById(R.id.locationTV);
        mWeatherImage = (ImageView) findViewById(R.id.weatherSymbolIV);
        mTemperatureLabel = (TextView) findViewById(R.id.tempTV);
        ImageButton changeCityButton = (ImageButton) findViewById(R.id.changeCityButton);


        //OnClickListener to the changeCityButton
        changeCityButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent myIntent = new Intent(WeatherController.this, ChangeCityController.class);
                startActivityForResult(myIntent, NEW_CITY_CODE);
            }
        });
    }

    //onResume() method
    //Deleted @Override
    protected void OnResume() {
        super.onResume();
        //Added logs to check if working in Logcat
        Log.d("EZWeather", "onResume() called");

        //Get new intent
        Intent myIntent = getIntent();
        String city = myIntent.getStringExtra("City");

        //If city is not null, get weather for new city, otherwise log message and get current weather
        if(city != null) {
            getWeatherForNewCity(city);
        } else {
            Log.d("EZWeather", "Getting weather for current location");
            getWeatherForCurrentLocation();
        }

        Log.d("EZWeather", "Getting weather for current location");
        getWeatherForCurrentLocation();
    }


    //getWeatherForNewCity(String city) method
    private void getWeatherForNewCity(String city) {

        RequestParams params = new RequestParams();
        params.put("q", city);
        params.put("appid", APP_ID);
        letsDoSomeNetworking(params);
    }


    //getWeatherForCurrentLocation() method
    private void getWeatherForCurrentLocation() {
        mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        mLocationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {

                //Added log to check if working in Logcat
                Log.d("EZWeather", "onLocationChanged() callback received");

                String longitude = String.valueOf(location.getLongitude());
                String latitude = String.valueOf(location.getLatitude());

                Log.d("EZWeather", "longitude is: " + longitude);
                Log.d("EZWeather", "latitude is: " + latitude);

                RequestParams params = new RequestParams();
                params.put("lat", latitude);
                params.put("lon", longitude);
                params.put("appid", APP_ID);
                letsDoSomeNetworking(params);
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {

                Log.d("EZWeather", "onProviderDisabled() callback received");

            }
        };

        //Autocode generated here
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            ActivityCompat.requestPermissions(this,
                    new String[] {Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_CODE);
            return;
        }
        mLocationManager.requestLocationUpdates(LOCATION_PROVIDER, MIN_TIME, MIN_DISTANCE, mLocationListener);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_CODE) {

            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d("EZWeather", "onRequestPermissionsResult(): Permission granted!");
                getWeatherForCurrentLocation();
            } else {
                Log.d("EZWeather", "Permission denied. Sad.");
            }
        }

    }

    //letsDoSomeNetworking(RequestParams params) method. This method utilizes the imported cz.msebera http client
    private void letsDoSomeNetworking(RequestParams params) {
        AsyncHttpClient client = new AsyncHttpClient();

        client.get(WEATHER_URL, params, new JsonHttpResponseHandler() {

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {

                Log.d("EZWeather", "Success! JSON: " + response.toString());
                WeatherDataModel weatherData = WeatherDataModel.fromJson(response);
                updateUI(weatherData);

            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable e, JSONObject response) {
                Log.e("EZWeather", "Fail " + e.toString());
                Log.d("EZWeather", "Status code " + statusCode);
                Toast.makeText(WeatherController.this, "Request Failed", Toast.LENGTH_SHORT).show();
            }
        });
    }


    //updateUI() method
    private void updateUI(WeatherDataModel weather) {
        mTemperatureLabel.setText(weather.getTemperature());
        mCityLabel.setText(weather.getCity());

        int resourceID = getResources().getIdentifier(weather.getIconName(), "drawable", getPackageName());
        mWeatherImage.setImageResource(resourceID);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Log.d(LOGCAT_TAG, "onActivityResult() called");
        if (requestCode == NEW_CITY_CODE) {
            if (resultCode == RESULT_OK) {
                String city = data.getStringExtra("City");
                Log.d(LOGCAT_TAG, "New city is " + city);

                mUseLocation = false;
                getWeatherForNewCity(city);
            }
        }
    }

    //onPause() method
    @Override
    protected void onPause() {
        super.onPause();

        if (mLocationManager != null) {
            mLocationManager.removeUpdates(mLocationListener);
        }
    }
}
