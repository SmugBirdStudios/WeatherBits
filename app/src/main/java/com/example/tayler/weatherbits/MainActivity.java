package com.example.tayler.weatherbits;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import butterknife.BindView;
import butterknife.ButterKnife;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import pl.droidsonroids.gif.GifDrawable;
import pl.droidsonroids.gif.GifImageView;

import static android.widget.Toast.makeText;

public class MainActivity extends AppCompatActivity {

    @BindView(R.id.weatherIcon) GifImageView weatherIcon;
    @BindView(R.id.degrees) TextView degrees;
    @BindView(R.id.humidity) TextView humidity;
    @BindView(R.id.precipChance) TextView precipChance;
    @BindView(R.id.precipText) TextView precipText;
    @BindView(R.id.humidText) TextView humidityText;
    @BindView(R.id.background) ConstraintLayout background;
    @BindView(R.id.swipeRefresh) SwipeRefreshLayout mSwipeRefreshLayout;


    private static final String DARK_SKY_KEY = BuildConfig.DARK_SKY_KEY;

    public static final String TAG = MainActivity.class.getSimpleName();

    private CurrentWeather mCurrentWeather;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        final double latitude =  39.167107;
        final double longitude = -86.534359;

        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getForecast(latitude, longitude);
                mSwipeRefreshLayout.setRefreshing(false);
            }
        });


        // setting a default font for app

        Typeface t = Typeface.createFromAsset(getAssets(), "fonts/StarPerv.ttf");
        TextView precipText = (TextView) findViewById(R.id.precipText);
        precipText.setTypeface(t);
        TextView degrees = (TextView) findViewById(R.id.degrees);
        degrees.setTypeface(t);
        TextView humidText = (TextView) findViewById(R.id.humidText);
        humidText.setTypeface(t);
        TextView precipChance = (TextView) findViewById(R.id.precipChance);
        precipChance.setTypeface(t);
        TextView humidity = (TextView) findViewById(R.id.humidity);
        humidity.setTypeface(t);




        getForecast(latitude, longitude);

    }

    private void getForecast(double latitude, double longitude) {
        String forecastUrl = "https://api.darksky.net/forecast/" + DARK_SKY_KEY + "/" + latitude + "," + longitude;

        if (isNetworkAvailable()) {

            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder()
                    .url(forecastUrl)
                    .build();

            Call call = client.newCall(request);
            call.enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {

                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {

                    try {
                        String jsonData =  response.body().string();
                        Log.v(TAG,jsonData);

                        if (response.isSuccessful()) {
                            mCurrentWeather = getCurrentDetails(jsonData);
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    updateDisplay();
                                }
                            });
                        } else {
                            alertUserAboutError();
                        }
                    } catch (IOException e) {
                        Log.e(TAG, "Exception caught: ", e);
                    } catch (JSONException e) {
                        Log.e(TAG, "Exception caught: ", e);
                    }
                }
            });
        }
        else {
            Toast.makeText(this, R.string.network_unavailable_message, Toast.LENGTH_LONG).show();
        }
    }

    private void updateDisplay() {
        degrees.setText(mCurrentWeather.getTemperature() + "");
        humidity.setText(mCurrentWeather.getHumidity() + "%");
        precipChance.setText(mCurrentWeather.getPrecipChance() + "%");
        weatherIcon.setImageResource(mCurrentWeather.getIconId());
    }

    private CurrentWeather getCurrentDetails(String jsonData) throws JSONException {
        JSONObject forecast = new JSONObject(jsonData);
        String timezone = forecast.getString("timezone");
        Log.i(TAG, "From JSON: " + timezone);

        JSONObject currently = forecast.getJSONObject("currently");

        CurrentWeather currentWeather = new CurrentWeather();
        currentWeather.setHumidity(currently.getDouble("humidity"));
        currentWeather.setTime(currently.getLong("time"));
        currentWeather.setIcon(currently.getString("icon"));
        currentWeather.setPrecipChance(currently.getDouble("precipProbability"));
        currentWeather.setSummary(currently.getString("summary"));
        currentWeather.setTemperature(currently.getDouble("temperature"));
        currentWeather.setTimeZone(timezone);

        Log.d(TAG, currentWeather.getFormattedTime());

        return currentWeather;
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager manager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo networkInfo = manager.getActiveNetworkInfo();
        boolean isAvailable = false;

        if (networkInfo != null && networkInfo.isConnected()) {
            isAvailable =true;
        }
        return isAvailable;
    }

    private void alertUserAboutError() {
        AlertDialogFragment dialog = new AlertDialogFragment();
        dialog.show(getFragmentManager(), "error_dialog");
    }


}
