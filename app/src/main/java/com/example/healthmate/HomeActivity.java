package com.example.healthmate;

import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;

import com.example.healthmate.databinding.ActivityHomeBinding;

import java.util.Arrays;
import java.util.List;

public class HomeActivity extends AppCompatActivity {
    private ActivityHomeBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityHomeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Start bg animation
        AnimationDrawable bg = (AnimationDrawable) binding.animatedBg.getBackground();
        bg.setEnterFadeDuration(3000);
        bg.setExitFadeDuration(3000);
        bg.start();

        // Feature list
        List<Feature> features = Arrays.asList(
                new Feature(R.drawable.ic_mood,          "Mood Tracker",   "Check in daily",      MoodTrackerActivity.class),
                new Feature(R.drawable.ic_journal,       "Journal",        "Write thoughts",      JournalActivity.class),
                new Feature(R.drawable.ic_steps,         "Step Counter",   "Track activity",      StepCounterActivity.class),
                new Feature(R.drawable.ic_local_drink,   "Water Tracker",  "Stay hydrated",       WaterTrackerActivity.class),
                new Feature(R.drawable.ic_breathe,       "Breathing",      "Relax now",           BreathingActivity.class),
                new Feature(R.drawable.ic_quote,         "Daily Quote",    "Inspiration",         QuoteActivity.class),
        new Feature(R.drawable.ic_habit,       "Habit Tracker",   "Build good habits",    HabitTrackerActivity.class),
                new Feature(R.drawable.ic_sleep,       "Sleep Log",       "Track your sleep",     SleepLogActivity.class),
                new Feature(R.drawable.ic_weather,     "Weather",         "Local forecast",       WeatherActivity.class)
        );


        CarouselAdapter adapter = new CarouselAdapter(features, feature ->
                startActivity(new Intent(this, feature.activityClass))
        );
        binding.carousel.setLayoutManager(new GridLayoutManager(this, 2));
        binding.carousel.setAdapter(adapter);


        binding.fabChat.setOnClickListener(v ->
                startActivity(new Intent(this, ChatActivity.class))
        );

        // Bottom Nav
        binding.bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                Toast.makeText(this, "Home tapped", Toast.LENGTH_SHORT).show();
            } else if (id == R.id.nav_chat) {
                startActivity(new Intent(this, ChatActivity.class));
            } else if (id == R.id.nav_profile) {
                startActivity(new Intent(this, ProfileActivity.class));
            }
            return true;
        });
    }
}
