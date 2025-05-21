package com.example.healthmate;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.healthmate.databinding.ActivityLoginBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class LoginActivity extends AppCompatActivity {
    private ActivityLoginBinding b;
    private FirebaseAuth auth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        b = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(b.getRoot());

        //  animation
        View[] views = { b.ivLogo, b.tvAppName, b.cardLogin };
        for (int i = 0; i < views.length; i++) {
            views[i].setAlpha(0f);
            views[i].animate()
                    .alpha(1f)
                    .setStartDelay(100L * i)
                    .setDuration(400)
                    .start();
        }

        auth = FirebaseAuth.getInstance();
        db   = FirebaseFirestore.getInstance();

        b.btnLogin.setOnClickListener(v -> {
            String u = b.etUsername.getText().toString().trim();
            String p = b.etPassword.getText().toString();
            if (u.isEmpty() || p.isEmpty()) {
                Toast.makeText(this, "Enter username & password", Toast.LENGTH_SHORT).show();
                return;
            }
            db.collection("usernames").document(u).get()
                    .addOnSuccessListener(doc -> {
                        if (!doc.exists()) {
                            Toast.makeText(this, "No such user", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        String uid = doc.getString("uid");
                        if (uid == null) {
                            Toast.makeText(this, "Mapping error", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        db.collection("users").document(uid).get()
                                .addOnSuccessListener(uDoc -> {
                                    String email = uDoc.getString("email");
                                    if (email == null) {
                                        Toast.makeText(this, "Email not found", Toast.LENGTH_SHORT).show();
                                        return;
                                    }
                                    auth.signInWithEmailAndPassword(email, p)
                                            .addOnSuccessListener(a -> openHome())
                                            .addOnFailureListener(e ->
                                                    Toast.makeText(this, "Login failed: " + e.getMessage(),
                                                            Toast.LENGTH_LONG).show()
                                            );
                                })
                                .addOnFailureListener(e ->
                                        Toast.makeText(this, "Profile error: " + e.getMessage(),
                                                Toast.LENGTH_LONG).show()
                                );
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(this, "Lookup error: " + e.getMessage(),
                                    Toast.LENGTH_LONG).show()
                    );
        });

        b.tvSignupLink.setOnClickListener(v ->
                startActivity(new Intent(this, SignupActivity.class))
        );
    }

    private void openHome() {
        startActivity(new Intent(this, HomeActivity.class));
        finish();
    }
}
