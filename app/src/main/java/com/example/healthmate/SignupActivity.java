package com.example.healthmate;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.healthmate.databinding.ActivitySignupBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;

import java.util.HashMap;
import java.util.Map;

public class SignupActivity extends AppCompatActivity {
    private ActivitySignupBinding b;
    private FirebaseAuth auth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        b = ActivitySignupBinding.inflate(getLayoutInflater());
        setContentView(b.getRoot());

        auth = FirebaseAuth.getInstance();
        db   = FirebaseFirestore.getInstance();

        b.btnSignup.setOnClickListener(v -> {
            String username = b.etUsername.getText().toString().trim();
            String email    = b.etEmail.getText().toString().trim();
            String pass     = b.etPassword.getText().toString();
            String confirm  = b.etConfirmPassword.getText().toString();

            if (username.isEmpty() || email.isEmpty() || pass.isEmpty() || confirm.isEmpty()) {
                Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show();
                return;
            }
            if (!pass.equals(confirm)) {
                Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
                return;
            }


            auth.createUserWithEmailAndPassword(email, pass)
                    .addOnSuccessListener(cred -> {
                        String uid = auth.getCurrentUser().getUid();


                        WriteBatch batch = db.batch();

                        Map<String,Object> usernameMap = new HashMap<>();
                        usernameMap.put("uid", uid);
                        batch.set(db.collection("usernames").document(username), usernameMap);

                        Map<String,Object> profileMap = new HashMap<>();
                        profileMap.put("email", email);
                        profileMap.put("username", username);
                        batch.set(db.collection("users").document(uid), profileMap);


                        batch.commit()
                                .addOnSuccessListener(unused -> {

                                    startActivity(new Intent(SignupActivity.this, HomeActivity.class));
                                    finish();
                                })
                                .addOnFailureListener(e ->
                                        Toast.makeText(this, "Error saving user data: " + e.getMessage(),
                                                Toast.LENGTH_LONG).show()
                                );
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(this, "Sign up failed: " + e.getMessage(), Toast.LENGTH_LONG).show()
                    );
        });

        b.tvLoginLink.setOnClickListener(v ->
                startActivity(new Intent(this, LoginActivity.class))
        );
    }
}
