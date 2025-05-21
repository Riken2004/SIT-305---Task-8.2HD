package com.example.healthmate;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;

import com.airbnb.lottie.LottieAnimationView;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.progressindicator.CircularProgressIndicator;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class WaterTrackerActivity extends AppCompatActivity {
    private static final String PREFS = "water_prefs";
    private static final String KEY_GOAL = "water_goal";
    private static final String KEY_PREFIX = "water_"; // will append yyyy-MM-dd

    private SharedPreferences prefs;
    private int goal, count;
    private String todayKey;

    private MaterialToolbar toolbar;
    private TextView tvCount;
    private CircularProgressIndicator progressRing;
    private LottieAnimationView animWater;
    private FloatingActionButton fabAdd, fabSub;
    private BarChart chartHistory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_water_tracker);


        prefs = getSharedPreferences(PREFS, MODE_PRIVATE);
        goal = prefs.getInt(KEY_GOAL, 8);
        todayKey = KEY_PREFIX
                + new SimpleDateFormat("yyyy-MM-dd", Locale.US)
                .format(Calendar.getInstance().getTime());
        count = prefs.getInt(todayKey, 0);


        toolbar      = findViewById(R.id.toolbar);
        tvCount      = findViewById(R.id.tvWaterCount);
        progressRing = findViewById(R.id.progressRing);
        animWater    = findViewById(R.id.animWater);
        fabAdd       = findViewById(R.id.fabAddWater);
        fabSub       = findViewById(R.id.fabSubWater);
        chartHistory = findViewById(R.id.chartWaterHistory);


        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());


        progressRing.setMax(goal);
        updateUI();
        loadHistory();


        fabAdd.setOnClickListener(v -> {
            if (count < goal) {
                count++;
                prefs.edit().putInt(todayKey, count).apply();
                animWater.playAnimation();
                updateUI();
                loadHistory();
            } else {
                Toast.makeText(this, "Goal reached!", Toast.LENGTH_SHORT).show();
            }
        });

        fabSub.setOnClickListener(v -> {
            if (count > 0) {
                count--;
                prefs.edit().putInt(todayKey, count).apply();
                updateUI();
                loadHistory();
            }
        });
    }

    private void updateUI() {
        tvCount.setText(count + " / " + goal + " glasses");
        progressRing.setProgress(count);
    }

    private void loadHistory() {
        List<BarEntry> entries = new ArrayList<>();
        List<String> labels    = new ArrayList<>();
        Calendar cal = Calendar.getInstance();

        for (int i = 6; i >= 0; i--) {
            cal.setTimeInMillis(System.currentTimeMillis());
            cal.add(Calendar.DATE, -i);
            String key = KEY_PREFIX
                    + new SimpleDateFormat("yyyy-MM-dd", Locale.US)
                    .format(cal.getTime());
            int val = prefs.getInt(key, 0);
            entries.add(new BarEntry(6 - i, val));
            labels.add(
                    new SimpleDateFormat("MM/dd", Locale.US)
                            .format(cal.getTime())
            );
        }

        BarDataSet set = new BarDataSet(entries, "Glasses");
        set.setColor(getColor(R.color.teal_200));
        BarData data = new BarData(set);
        data.setBarWidth(0.5f);

        chartHistory.setData(data);
        chartHistory.getDescription().setEnabled(false);
        XAxis x = chartHistory.getXAxis();
        x.setValueFormatter(new IndexAxisValueFormatter(labels));
        x.setGranularity(1f);
        x.setPosition(XAxis.XAxisPosition.BOTTOM);
        chartHistory.getAxisRight().setEnabled(false);
        chartHistory.getAxisLeft().setAxisMinimum(0f);
        chartHistory.invalidate();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_water_tracker, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_settings) {
            showGoalDialog();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showGoalDialog() {
        EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        input.setText(String.valueOf(goal));

        new AlertDialog.Builder(this)
                .setTitle("Set Daily Goal")
                .setView(input)
                .setPositiveButton("Save", (dialog, which) -> {
                    try {
                        int g = Integer.parseInt(input.getText().toString());
                        if (g > 0) {
                            goal = g;
                            prefs.edit().putInt(KEY_GOAL, goal).apply();
                            progressRing.setMax(goal);
                            updateUI();
                        }
                    } catch (NumberFormatException ignored) {}
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
