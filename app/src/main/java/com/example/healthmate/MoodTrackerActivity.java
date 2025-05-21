package com.example.healthmate;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.*;

public class MoodTrackerActivity extends AppCompatActivity {
    private final String[] moods = {"😞","😐","🙂","😄","🤩"};
    private TextView tvSelectedMood;
    private RecyclerView rvHistory;
    private MoodHistoryAdapter adapter;
    private FirebaseFirestore db;
    private String uid;

    private MaterialCardView cvBreathingPrompt;
    private Button btnStartBreathing, btnJournalLink;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mood_tracker);


        Toolbar tb = findViewById(R.id.toolbar);
        setSupportActionBar(tb);
        tb.setNavigationOnClickListener(v -> finish());


        tvSelectedMood    = findViewById(R.id.tvSelectedMood);
        rvHistory         = findViewById(R.id.rvMoodHistory);
        cvBreathingPrompt = findViewById(R.id.cvBreathingPrompt);
        btnStartBreathing = findViewById(R.id.btnStartBreathing);
        btnJournalLink    = findViewById(R.id.btnJournalLink);

        //  Firestore & user
        db  = FirebaseFirestore.getInstance();
        uid = FirebaseAuth.getInstance().getUid();


        adapter = new MoodHistoryAdapter();
        rvHistory.setLayoutManager(
                new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        );
        rvHistory.setAdapter(adapter);
        loadMoodHistory();



        // Mood buttons
        int[] btns = { R.id.btnMood1, R.id.btnMood2, R.id.btnMood3,
                R.id.btnMood4, R.id.btnMood5 };
        for (int i = 0; i < btns.length; i++) {
            final int idx = i;
            findViewById(btns[i]).setOnClickListener(v -> recordMood(idx));
        }

        // Breathing prompt button
        btnStartBreathing.setOnClickListener(v ->
                startActivity(new Intent(this, BreathingActivity.class))
        );

        // Journal link
        btnJournalLink.setOnClickListener(v ->
                startActivity(new Intent(this, JournalActivity.class))
        );
    }

    private void recordMood(int idx) {
        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.US)
                .format(new Date());
        String emoji = moods[idx];
        tvSelectedMood.setText(emoji);

        db.collection("users").document(uid)
                .collection("moods").document(today)
                .set(Collections.singletonMap("mood", emoji))
                .addOnSuccessListener(a -> {
                    Snackbar.make(tvSelectedMood, "Saved " + emoji,
                            Snackbar.LENGTH_SHORT).show();
                    loadMoodHistory();
                })
                .addOnFailureListener(e ->
                        Snackbar.make(tvSelectedMood, "Save failed",
                                Snackbar.LENGTH_SHORT).show());
    }

    private void loadMoodHistory() {

        List<MoodHistoryAdapter.MoodEntry> list = new ArrayList<>();
        Calendar cal = Calendar.getInstance();
        List<String> dates = new ArrayList<>();

        for (int i = 6; i >= 0; i--) {
            cal.setTime(new Date());
            cal.add(Calendar.DATE, -i);
            String d = new SimpleDateFormat("yyyy-MM-dd", Locale.US)
                    .format(cal.getTime());
            list.add(new MoodHistoryAdapter.MoodEntry(d, "…"));
            dates.add(d);
        }
        adapter.setEntries(list);


        db.collection("users").document(uid)
                .collection("moods")
                .whereIn(FieldPath.documentId(), dates)
                .get()
                .addOnSuccessListener(snap -> {
                    String todayKey = dates.get(dates.size()-1);
                    String todayMood = null;

                    for (DocumentSnapshot doc : snap.getDocuments()) {
                        String d = doc.getId();
                        String m = doc.getString("mood");
                        adapter.updateMood(d, m);
                        if (d.equals(todayKey)) {
                            todayMood = m;
                        }
                    }


                    if (todayMood != null) {
                        int idx = Arrays.asList(moods).indexOf(todayMood);
                        if (idx >= 0 && idx <= 1) {
                            cvBreathingPrompt.setVisibility(View.VISIBLE);
                        } else {
                            cvBreathingPrompt.setVisibility(View.GONE);
                        }
                    }
                });
    }


}
