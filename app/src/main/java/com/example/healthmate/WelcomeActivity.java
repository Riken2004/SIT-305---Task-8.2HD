package com.example.healthmate;

import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.airbnb.lottie.LottieAnimationView;
import com.example.healthmate.databinding.ActivityWelcomeBinding;

public class WelcomeActivity extends AppCompatActivity {
    private ActivityWelcomeBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityWelcomeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Start animated gradient bg
        AnimationDrawable bgDrawable =
                (AnimationDrawable) binding.animatedBg.getBackground();
        bgDrawable.setEnterFadeDuration(3000);
        bgDrawable.setExitFadeDuration(3000);
        bgDrawable.start();


        LottieAnimationView hero = binding.heroLottie;
        hero.setAnimation("hero_futuristic.json");


        binding.btnGetStarted.setOnClickListener(v -> {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });
    }
}
