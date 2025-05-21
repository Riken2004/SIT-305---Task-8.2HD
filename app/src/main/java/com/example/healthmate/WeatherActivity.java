package com.example.healthmate;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class WeatherActivity extends AppCompatActivity {
    private Toolbar    toolbar;
    private ImageView  ivWeatherIcon;
    private TextView   tvWeatherTemp,
            tvWeatherDesc,
            tvWeatherHumidity,
            tvWeatherWind,
            tvWeatherUpdated;
    private ProgressBar weatherProgress;
    private EditText    etCity;
    private Button      btnSearch;
    private Call        currentCall;

    private static final String OWM_API_KEY = "1308613d93f2d8f0e978d906b9abb4d5";

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather);


        toolbar           = findViewById(R.id.toolbar);
        ivWeatherIcon     = findViewById(R.id.ivWeatherIcon);
        tvWeatherTemp     = findViewById(R.id.tvWeatherTemp);
        tvWeatherDesc     = findViewById(R.id.tvWeatherDesc);
        tvWeatherHumidity = findViewById(R.id.tvWeatherHumidity);
        tvWeatherWind     = findViewById(R.id.tvWeatherWind);
        tvWeatherUpdated  = findViewById(R.id.tvWeatherUpdated);
        weatherProgress   = findViewById(R.id.weatherProgress);
        etCity            = findViewById(R.id.etCity);
        btnSearch         = findViewById(R.id.btnSearch);


        setSupportActionBar(toolbar);
        if (getSupportActionBar()!=null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());


        btnSearch.setOnClickListener(v -> {
            String city = etCity.getText().toString().trim();
            if (!TextUtils.isEmpty(city)) {
                fetchWeather(city);
                etCity.clearFocus();
            }
        });

        etCity.setOnEditorActionListener((tv, actionId, ev) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                btnSearch.performClick();
                return true;
            }
            return false;
        });

        //Initial load
        etCity.setText("Melbourne");
        fetchWeather("Melbourne");
    }

    private void fetchWeather(String city) {

        if (currentCall != null) currentCall.cancel();


        weatherProgress.setVisibility(ProgressBar.VISIBLE);
        toolbar.setTitle("…");
        tvWeatherTemp.setText("--°C");
        tvWeatherDesc.setText("");
        tvWeatherHumidity.setText("--%");
        tvWeatherWind.setText("--");
        tvWeatherUpdated.setText("--:--");


        String url = "https://api.openweathermap.org/data/2.5/weather"
                + "?q=" + city
                + "&units=metric"
                + "&appid=" + OWM_API_KEY;

        OkHttpClient client = new OkHttpClient();
        Request req = new Request.Builder().url(url).build();
        currentCall = client.newCall(req);

        currentCall.enqueue(new Callback() {
            @Override public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> {
                    weatherProgress.setVisibility(ProgressBar.GONE);
                    toolbar.setTitle("Error");
                });
            }
            @Override public void onResponse(Call call, Response resp) throws IOException {
                String body = resp.body().string();
                runOnUiThread(() -> weatherProgress.setVisibility(ProgressBar.GONE));
                if (!resp.isSuccessful()) {
                    runOnUiThread(() -> toolbar.setTitle("Not found"));
                    return;
                }
                try {
                    JSONObject j        = new JSONObject(body);
                    String name         = j.getString("name");
                    JSONObject main     = j.getJSONObject("main");
                    double temp         = main.getDouble("temp");
                    int humidity        = main.getInt("humidity");
                    double windSpeed    = j.getJSONObject("wind").getDouble("speed");
                    JSONObject w0       = j.getJSONArray("weather").getJSONObject(0);
                    String desc         = w0.getString("description");
                    String icon         = w0.getString("icon");
                    String updated = new SimpleDateFormat("HH:mm, dd MMM", Locale.US)
                            .format(new Date());

                    runOnUiThread(() -> {
                        toolbar.setTitle(name);
                        tvWeatherTemp.setText(String.format(Locale.US,"%.0f°C",temp));
                        tvWeatherDesc.setText(
                                desc.substring(0,1).toUpperCase()+desc.substring(1)
                        );
                        tvWeatherHumidity.setText(humidity + "%");
                        tvWeatherWind.setText(String.format(Locale.US,"%.1f m/s",windSpeed));
                        tvWeatherUpdated.setText(updated);
                        String iconUrl = "https://openweathermap.org/img/wn/"+icon+"@2x.png";
                        Glide.with(WeatherActivity.this)
                                .load(iconUrl)
                                .into(ivWeatherIcon);
                    });
                } catch (Exception ex) {
                    ex.printStackTrace();
                    runOnUiThread(() -> toolbar.setTitle("Parse error"));
                }
            }
        });
    }

    @Override protected void onDestroy() {
        if (currentCall!=null) currentCall.cancel();
        super.onDestroy();
    }
}
