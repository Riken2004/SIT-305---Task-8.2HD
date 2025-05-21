package com.example.healthmate;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.TimePicker;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Dao;
import androidx.room.Database;
import androidx.room.Entity;
import androidx.room.Insert;
import androidx.room.PrimaryKey;
import androidx.room.Query;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class SleepLogActivity extends AppCompatActivity {

    @Entity
    public static class SleepEntry {
        @PrimaryKey(autoGenerate = true) public long id;
        public long dateMillis;
        public int bedMinutes, wakeMinutes;
    }

    @Dao
    interface SleepDao {
        @Insert void insert(SleepEntry e);
        @Query("SELECT * FROM SleepEntry ORDER BY dateMillis DESC") List<SleepEntry> getAll();
    }

    @Database(entities = SleepEntry.class, version = 1, exportSchema = false)
    public abstract static class SleepDatabase extends RoomDatabase {
        public abstract SleepDao sleepDao();
    }


    public static class SleepAlarmReceiver extends BroadcastReceiver {
        @Override public void onReceive(Context ctx, Intent intent) {
            String msg = intent.getStringExtra("msg");
            NotificationManager nm =
                    (NotificationManager)ctx.getSystemService(Context.NOTIFICATION_SERVICE);
            NotificationCompat.Builder nb = new NotificationCompat.Builder(ctx, "sleep")
                    .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
                    .setContentTitle("Sleep Reminder")
                    .setContentText(msg)
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setAutoCancel(true);
            nm.notify((int)System.currentTimeMillis(), nb.build());
        }
    }

    private TextInputEditText etBed, etWake;
    private MaterialButton btnSave;
    private BarChart chart;
    private RecyclerView rvHistory;
    private SleepDatabase db;
    private HistoryAdapter adapter;
    private List<SleepEntry> historyList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sleep_log);

        etBed     = findViewById(R.id.etBedTime);
        etWake    = findViewById(R.id.etWakeTime);
        btnSave   = findViewById(R.id.btnSave);
        chart     = findViewById(R.id.chartSleep);
        rvHistory = findViewById(R.id.rvHistory);


        db = Room.databaseBuilder(getApplicationContext(),
                        SleepDatabase.class, "sleep-db")
                .allowMainThreadQueries()
                .build();
        historyList.addAll(db.sleepDao().getAll());

        adapter = new HistoryAdapter(historyList);
        rvHistory.setLayoutManager(new LinearLayoutManager(this));
        rvHistory.setAdapter(adapter);

        // Initial chart
        updateChart();


        etBed.setOnClickListener(v -> pickTime((h, m) ->
                etBed.setText(String.format("%02d:%02d", h, m))
        ));
        etWake.setOnClickListener(v -> pickTime((h, m) ->
                etWake.setText(String.format("%02d:%02d", h, m))
        ));


        btnSave.setOnClickListener(v -> saveSleep());


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel chan = new NotificationChannel(
                    "sleep","Sleep Reminders", NotificationManager.IMPORTANCE_HIGH);
            ((NotificationManager)getSystemService(NOTIFICATION_SERVICE))
                    .createNotificationChannel(chan);
        }
    }

    private void pickTime(TimeCallback cb) {
        Calendar c = Calendar.getInstance();
        new TimePickerDialog(this,
                (TimePicker view, int hour, int minute) -> cb.onTime(hour, minute),
                c.get(Calendar.HOUR_OF_DAY),
                c.get(Calendar.MINUTE),
                true
        ).show();
    }

    private void saveSleep() {
        String b = etBed.getText().toString(), w = etWake.getText().toString();
        if (b.isEmpty() || w.isEmpty()) return;

        int bedM = toMinutes(b), wakeM = toMinutes(w);
        long now = System.currentTimeMillis();

        SleepEntry e = new SleepEntry();
        e.dateMillis  = now;
        e.bedMinutes  = bedM;
        e.wakeMinutes = wakeM;
        db.sleepDao().insert(e);
        historyList.add(0, e);
        adapter.notifyItemInserted(0);


        scheduleAlarm(bedM, 100, "Time to wind down for bed");
        scheduleAlarm(wakeM, 200, "Good morning! Time to wake up");

        updateChart();
    }

    private int toMinutes(String hhmm) {
        String[] p = hhmm.split(":");
        return Integer.parseInt(p[0]) * 60 + Integer.parseInt(p[1]);
    }

    private void scheduleAlarm(int minutesOfDay, int reqCode, String msg) {
        Calendar c = Calendar.getInstance();
        c.set(Calendar.HOUR_OF_DAY, minutesOfDay / 60);
        c.set(Calendar.MINUTE,       minutesOfDay % 60);
        long timeMs = c.getTimeInMillis();

        Intent i = new Intent(this, SleepAlarmReceiver.class)
                .putExtra("msg", msg);
        PendingIntent pi = PendingIntent.getBroadcast(
                this, reqCode, i,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        AlarmManager am = (AlarmManager)getSystemService(ALARM_SERVICE);
        am.set(AlarmManager.RTC_WAKEUP, timeMs, pi);
    }

    private void updateChart() {
        List<BarEntry> entries = new ArrayList<>();
        int idx = 0;
        for (SleepEntry e : historyList) {
            float hours = (e.wakeMinutes - e.bedMinutes) / 60f;
            entries.add(new BarEntry(idx++, hours));
            if (idx >= 7) break;
        }
        BarDataSet ds = new BarDataSet(entries, "Hours slept");
        ds.setColor(Color.parseColor("#00897B"));
        chart.setData(new BarData(ds));
        chart.getDescription().setEnabled(false);
        chart.invalidate();
    }

    interface TimeCallback { void onTime(int h, int m); }


    class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.VH> {
        private final List<SleepEntry> list;
        HistoryAdapter(List<SleepEntry> l) { list = l; }

        @NonNull @Override
        public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_sleep_entry, parent, false);
            return new VH(v);
        }

        @Override
        public void onBindViewHolder(@NonNull VH holder, int position) {
            SleepEntry e = list.get(position);
            Calendar c = Calendar.getInstance();
            c.setTimeInMillis(e.dateMillis);
            holder.tvDate.setText(
                    String.format("%02d/%02d/%04d",
                            c.get(Calendar.DAY_OF_MONTH),
                            c.get(Calendar.MONTH) + 1,
                            c.get(Calendar.YEAR))
            );
            holder.tvTimes.setText(
                    String.format("%02d:%02d → %02d:%02d",
                            e.bedMinutes/60, e.bedMinutes%60,
                            e.wakeMinutes/60, e.wakeMinutes%60)
            );
        }

        @Override public int getItemCount() { return list.size(); }

        class VH extends RecyclerView.ViewHolder {
            TextView tvDate, tvTimes;
            VH(@NonNull View itemView) {
                super(itemView);
                tvDate  = itemView.findViewById(R.id.tvDate);
                tvTimes = itemView.findViewById(R.id.tvTimes);
            }
        }
    }
}
