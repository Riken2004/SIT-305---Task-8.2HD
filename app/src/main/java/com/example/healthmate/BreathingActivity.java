package com.example.healthmate;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.widget.Button;
import android.widget.Switch;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.airbnb.lottie.LottieAnimationView;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.google.android.material.progressindicator.CircularProgressIndicator;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class BreathingActivity extends AppCompatActivity {
    private static final String PREFS             = "BreathingPrefs";
    private static final String KEY_COUNT_PREFIX  = "count_";
    private static final String KEY_DARK_MODE     = "dark_mode";

    // Timing constants
    private static final long INHALE_MS   = 4000;
    private static final long HOLD_MS     = 4000;
    private static final long EXHALE_MS   = 4000;
    private static final long SESSION_MS  = 60_000;

    private SharedPreferences prefs;
    private Switch switchDarkMode;
    private BarChart chartSessions;
    private TextView tvPrompt;
    private LottieAnimationView animBreathing;
    private CircularProgressIndicator progressBreathing;
    private Button btnStart;

    private final SimpleDateFormat keyFormatter =
            new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    private final SimpleDateFormat labelFormatter =
            new SimpleDateFormat("MM/dd", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        prefs = getSharedPreferences(PREFS, MODE_PRIVATE);
        boolean dark = prefs.getBoolean(KEY_DARK_MODE, false);
        AppCompatDelegate.setDefaultNightMode(
                dark ? AppCompatDelegate.MODE_NIGHT_YES
                        : AppCompatDelegate.MODE_NIGHT_NO
        );

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_breathing);


        switchDarkMode     = findViewById(R.id.switchDarkMode);
        chartSessions      = findViewById(R.id.chartSessions);
        tvPrompt           = findViewById(R.id.tvPrompt);
        animBreathing      = findViewById(R.id.animBreathing);
        progressBreathing  = findViewById(R.id.progressBreathing);
        btnStart           = findViewById(R.id.btnStart);

        // Dark‐mode switch listener
        switchDarkMode.setChecked(dark);
        switchDarkMode.setOnCheckedChangeListener((sw, isChecked) -> {
            prefs.edit().putBoolean(KEY_DARK_MODE, isChecked).apply();
            AppCompatDelegate.setDefaultNightMode(
                    isChecked ? AppCompatDelegate.MODE_NIGHT_YES
                            : AppCompatDelegate.MODE_NIGHT_NO
            );
        });

        // Initial UI
        setupSessionChart();
        tvPrompt.setText(R.string.ready_prompt);
        animBreathing.setVisibility(android.view.View.GONE);
        progressBreathing.setProgress(0);

        btnStart.setOnClickListener(v -> startBreathingSession());
    }

    private void setupSessionChart() {
        // Ensure correct timezone
        keyFormatter.setTimeZone(TimeZone.getDefault());
        labelFormatter.setTimeZone(TimeZone.getDefault());

        ArrayList<BarEntry> entries = new ArrayList<>();
        ArrayList<String> labels    = new ArrayList<>();

        Calendar cal = Calendar.getInstance();
        for (int i = 6; i >= 0; i--) {
            cal.setTime(new Date());
            cal.add(Calendar.DAY_OF_YEAR, -i);
            Date day    = cal.getTime();
            String key  = keyFormatter.format(day);
            String label= labelFormatter.format(day);
            int count   = prefs.getInt(KEY_COUNT_PREFIX + key, 0);

            entries.add(new BarEntry(6 - i, count));
            labels.add(label);
        }

        BarDataSet set = new BarDataSet(entries, "Sessions");
        BarData data = new BarData(set);
        data.setBarWidth(0.6f);

        chartSessions.setData(data);
        chartSessions.getDescription().setEnabled(false);

        XAxis x = chartSessions.getXAxis();
        x.setValueFormatter(new IndexAxisValueFormatter(labels));
        x.setGranularity(1f);
        x.setPosition(XAxis.XAxisPosition.BOTTOM);

        chartSessions.getAxisRight().setEnabled(false);
        chartSessions.getAxisLeft().setAxisMinimum(0f);

        chartSessions.invalidate();
    }

    private void startBreathingSession() {
        animBreathing.setVisibility(android.view.View.VISIBLE);
        animBreathing.playAnimation();
        btnStart.setEnabled(false);


        new CountDownTimer(SESSION_MS, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                long elapsed = SESSION_MS - millisUntilFinished;
                int percent = (int)(elapsed * 100 / SESSION_MS);
                progressBreathing.setProgress(percent);


                long cycle = INHALE_MS + HOLD_MS + EXHALE_MS;
                long pos = elapsed % cycle;
                if (pos < INHALE_MS) {
                    tvPrompt.setText("Breathe In");
                } else if (pos < INHALE_MS + HOLD_MS) {
                    tvPrompt.setText("Hold");
                } else {
                    tvPrompt.setText("Breathe Out");
                }
            }

            @Override
            public void onFinish() {
                progressBreathing.setProgress(100);
                animBreathing.pauseAnimation();
                btnStart.setEnabled(true);
                tvPrompt.setText("Done!");


                String todayKey = keyFormatter.format(new Date());
                int prev = prefs.getInt(KEY_COUNT_PREFIX + todayKey, 0);
                prefs.edit().putInt(KEY_COUNT_PREFIX + todayKey, prev + 1).apply();

                // Refresh chart
                setupSessionChart();
            }
        }.start();
    }
}
