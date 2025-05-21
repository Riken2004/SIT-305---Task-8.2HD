package com.example.healthmate;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.drawable.GradientDrawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.preference.PreferenceManager;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.google.android.material.progressindicator.CircularProgressIndicator;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class StepCounterActivity extends AppCompatActivity implements SensorEventListener {
    private static final String PREFS = "step_prefs", KEY_OFFSET = "step_offset";

    private SensorManager sensorMgr;
    private Sensor stepSensor;
    private long initialOffset;
    private int simulatedSteps = 0;

    private CircularProgressIndicator ring;
    private TextView tvSteps;
    private BarChart chart;
    private Button btnSimulate;

    private SharedPreferences prefs;
    private DBHelper db;

    @Override protected void onCreate(Bundle s) {
        super.onCreate(s);


        GradientDrawable gd = new GradientDrawable(
                GradientDrawable.Orientation.TOP_BOTTOM,
                new int[]{0xFFFFF8E1, 0xFFFFECB3}
        );
        getWindow().getDecorView().setBackground(gd);

        setContentView(R.layout.activity_step_counter);


        Toolbar tb = findViewById(R.id.toolbar);
        setSupportActionBar(tb);
        tb.setNavigationOnClickListener(v -> finish());

        ring       = findViewById(R.id.progressRing);
        tvSteps    = findViewById(R.id.tvSteps);
        chart      = findViewById(R.id.chartSteps);
        btnSimulate= findViewById(R.id.btnSimulate);

        prefs = getSharedPreferences(PREFS, MODE_PRIVATE);
        db    = new DBHelper(this);
        initialOffset = prefs.getLong(KEY_OFFSET, 0);


        SharedPreferences def = PreferenceManager.getDefaultSharedPreferences(this);
        int goal = Integer.parseInt(def.getString("pref_goal","10000"));
        String unit = def.getString("pref_unit","steps");
        ring.setMax(goal);


        sensorMgr  = (SensorManager) getSystemService(SENSOR_SERVICE);
        stepSensor = sensorMgr.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);


        if (isEmulator()) {
            btnSimulate.setVisibility(View.VISIBLE);
            btnSimulate.setOnClickListener(v -> {
                simulatedSteps++;
                update( simulatedSteps, goal, unit );
            });
        }


        updateChart();
    }

    @Override protected void onResume() {
        super.onResume();
        sensorMgr.registerListener(this, stepSensor, SensorManager.SENSOR_DELAY_UI);

        updateChart();
    }

    @Override protected void onPause() {
        super.onPause();
        sensorMgr.unregisterListener(this);
    }

    @Override public void onSensorChanged(SensorEvent e) {
        if (initialOffset==0) {
            initialOffset = (long)e.values[0];
            prefs.edit().putLong(KEY_OFFSET, initialOffset).apply();
        }
        int steps = (int)((long)e.values[0] - initialOffset) + simulatedSteps;
        SharedPreferences def = PreferenceManager.getDefaultSharedPreferences(this);
        int goal = Integer.parseInt(def.getString("pref_goal","10000"));
        String unit = def.getString("pref_unit","steps");
        update( steps, goal, unit );
    }

    private void update(int steps, int goal, String unit) {
        // Display
        if ("km".equals(unit)) {
            double km = steps * 0.0008;
            tvSteps.setText(new DecimalFormat("#0.00").format(km) + " km");
        } else {
            tvSteps.setText(String.valueOf(steps));
        }
        ring.setProgress(steps);

        String today = new SimpleDateFormat("yyyy-MM-dd",Locale.US)
                .format(System.currentTimeMillis());
        db.upsert(today, steps);
        updateChart();
    }

    private void updateChart() {
        List<BarEntry> entries = new ArrayList<>();
        List<String> labels = new ArrayList<>();
        Calendar cal = Calendar.getInstance();
        for (int i=6; i>=0; i--) {
            cal.setTimeInMillis(System.currentTimeMillis());
            cal.add(Calendar.DATE, -i);
            String d = new SimpleDateFormat("yyyy-MM-dd",Locale.US).format(cal.getTime());
            int cnt = db.query(d);
            entries.add(new BarEntry(6-i, cnt));
            labels.add(new SimpleDateFormat("MM/dd",Locale.US).format(cal.getTime()));
        }
        BarDataSet set = new BarDataSet(entries, "Steps");
        set.setColor(0xFF03DAC5);
        BarData data = new BarData(set);
        data.setBarWidth(0.6f);
        chart.setData(data);
        chart.getDescription().setEnabled(false);
        XAxis x = chart.getXAxis();
        x.setValueFormatter(new IndexAxisValueFormatter(labels));
        x.setGranularity(1f);
        x.setPosition(XAxis.XAxisPosition.BOTTOM);
        chart.getAxisRight().setEnabled(false);
        chart.getAxisLeft().setAxisMinimum(0f);
        chart.invalidate();
    }

    @Override public boolean onCreateOptionsMenu(Menu m) {
        getMenuInflater().inflate(R.menu.step_counter_menu, m);
        return true;
    }

    @Override public boolean onOptionsItemSelected(MenuItem i) {
        if (i.getItemId()==R.id.action_settings) {
            startActivity(new Intent(this,SettingsActivity.class));
            return true;
        } else if (i.getItemId()==R.id.action_map) {
            startActivity(new Intent(this,MapActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(i);
    }

    @Override public void onAccuracyChanged(Sensor s,int a){}

    private boolean isEmulator() {
        return Build.FINGERPRINT.startsWith("generic")
                || Build.MODEL.contains("Emulator")
                || Build.MANUFACTURER.contains("Genymotion")
                || (Build.BRAND.startsWith("generic") && Build.DEVICE.startsWith("generic"))
                || "google_sdk".equals(Build.PRODUCT);
    }


    private static class DBHelper extends SQLiteOpenHelper {
        private static final String DB="steps.db", T="daily_steps";
        DBHelper(StepCounterActivity c){ super(c,DB,null,1); }
        @Override public void onCreate(SQLiteDatabase db){
            db.execSQL("CREATE TABLE "+T+"(date TEXT PRIMARY KEY,steps INTEGER)");
        }
        @Override public void onUpgrade(SQLiteDatabase db,int o,int n){
            db.execSQL("DROP TABLE IF EXISTS "+T);
            onCreate(db);
        }
        void upsert(String date,int steps){
            SQLiteDatabase db = getWritableDatabase();
            android.content.ContentValues cv = new android.content.ContentValues();
            cv.put("date", date);
            cv.put("steps", steps);
            db.insertWithOnConflict(T,null,cv,SQLiteDatabase.CONFLICT_REPLACE);
        }
        int query(String date){
            SQLiteDatabase db = getReadableDatabase();
            Cursor c = db.rawQuery("SELECT steps FROM "+T+" WHERE date=?",
                    new String[]{date});
            int out=0;
            if(c.moveToFirst()) out=c.getInt(0);
            c.close();
            return out;
        }
    }
}
