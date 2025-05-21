package com.example.healthmate;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.healthmate.databinding.ActivityProfileBinding;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.util.Calendar;

public class ProfileActivity extends AppCompatActivity {
    private ActivityProfileBinding b;
    private SharedPreferences prefs;

    private static final String PREFS = "healthmate_prefs";
    private static final String KEY_2FA = "two_factor_enabled";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        b = ActivityProfileBinding.inflate(getLayoutInflater());
        setContentView(b.getRoot());

        prefs = getSharedPreferences(PREFS, MODE_PRIVATE);

        setSupportActionBar(b.toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        b.tvName.setText("Riken Patel");
        b.tvEmail.setText("patelriken2004@gmail.com");
        b.tvBio.setText("Android dev & fitness enthusiast...");
        b.tvLocation.setText("Melbourne, AUS");
        b.tvPhone.setText("+61 412 345 678");
        b.tvBirthday.setText("25/07/2004");

        // Facebook link
        b.btnLinkFacebook.setOnClickListener(v -> openFacebook("1000123456789"));

        // Share button
        b.btnShareProfile.setOnClickListener(v -> {
            Intent share = new Intent(Intent.ACTION_SEND);
            share.setType("text/plain");
            share.putExtra(Intent.EXTRA_SUBJECT, "Check out my profile on HealthMate");
            share.putExtra(Intent.EXTRA_TEXT,
                    "View my profile: https://yourapp.com/user/rikenpatel");
            startActivity(Intent.createChooser(share, "Share via"));
        });

        // Change Password
        b.btnChangePassword.setOnClickListener(v -> showChangePasswordDialog());


        boolean twoFactorEnabled = prefs.getBoolean(KEY_2FA, false);
        b.switch2fa.setChecked(twoFactorEnabled);
        b.switch2fa.setOnCheckedChangeListener((btn, isChecked) -> {
            prefs.edit().putBoolean(KEY_2FA, isChecked).apply();
            Toast.makeText(
                    this,
                    isChecked ? "2-Factor Authentication ENABLED"
                            : "2-Factor Authentication DISABLED",
                    Toast.LENGTH_SHORT
            ).show();
        });

        // Delete Account
        b.btnDeleteAccount.setOnClickListener(v -> showDeleteAccountDialog());
    }

    private void openFacebook(String pageId) {
        Intent intent;
        if (isPackageInstalled("com.facebook.katana")) {
            intent = new Intent(Intent.ACTION_VIEW, Uri.parse("fb://page/" + pageId));
        } else {
            intent = new Intent(Intent.ACTION_VIEW,
                    Uri.parse("https://www.facebook.com/" + pageId));
        }
        startActivity(intent);
    }

    private boolean isPackageInstalled(String pkgName) {
        try {
            getPackageManager().getPackageInfo(pkgName, 0);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_profile, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish(); return true;
        }
        if (item.getItemId() == R.id.action_edit) {
            showEditSheet(); return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showEditSheet() {
        BottomSheetDialog dialog = new BottomSheetDialog(this);
        View sheet = getLayoutInflater().inflate(R.layout.sheet_edit_profile, null);
        dialog.setContentView(sheet);

        EditText etName      = sheet.findViewById(R.id.etName);
        EditText etEmail     = sheet.findViewById(R.id.etEmail);
        EditText etBio       = sheet.findViewById(R.id.etBio);
        EditText etPhone     = sheet.findViewById(R.id.etPhone);
        EditText etBirthdate = sheet.findViewById(R.id.etBirthdate);
        View    layoutBirth  = sheet.findViewById(R.id.layoutBirthdate);
        View    btnSave      = sheet.findViewById(R.id.btnSave);

        etName.setText(b.tvName.getText());
        etEmail.setText(b.tvEmail.getText());
        etBio.setText(b.tvBio.getText());
        etPhone.setText(b.tvPhone.getText());
        etBirthdate.setText(b.tvBirthday.getText());

        View.OnClickListener pickDate = v -> {
            Calendar c = Calendar.getInstance();
            new DatePickerDialog(this,
                    (dp, year, month, day) ->
                            etBirthdate.setText(
                                    String.format("%02d/%02d/%04d", day, month+1, year)
                            ),
                    c.get(Calendar.YEAR),
                    c.get(Calendar.MONTH),
                    c.get(Calendar.DAY_OF_MONTH)
            ).show();
        };
        etBirthdate.setOnClickListener(pickDate);
        layoutBirth.setOnClickListener(pickDate);

        btnSave.setOnClickListener(v -> {
            b.tvName.setText(etName.getText());
            b.tvEmail.setText(etEmail.getText());
            b.tvBio.setText(etBio.getText());
            b.tvPhone.setText(etPhone.getText());
            b.tvBirthday.setText(etBirthdate.getText());
            dialog.dismiss();
        });

        dialog.show();
    }

    private void showChangePasswordDialog() {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        int pad = (int)(16 * getResources().getDisplayMetrics().density);
        layout.setPadding(pad, pad, pad, pad);

        EditText etNew = new EditText(this);
        etNew.setHint("New Password");
        etNew.setInputType(InputType.TYPE_CLASS_TEXT |
                InputType.TYPE_TEXT_VARIATION_PASSWORD);
        layout.addView(etNew);

        EditText etConfirm = new EditText(this);
        etConfirm.setHint("Confirm Password");
        etConfirm.setInputType(InputType.TYPE_CLASS_TEXT |
                InputType.TYPE_TEXT_VARIATION_PASSWORD);
        layout.addView(etConfirm);

        new AlertDialog.Builder(this)
                .setTitle("Change Password")
                .setView(layout)
                .setPositiveButton("Save", (dlg, which) -> {
                    String p1 = etNew.getText().toString();
                    String p2 = etConfirm.getText().toString();
                    if (p1.isEmpty() || !p1.equals(p2)) {
                        Toast.makeText(this, "Passwords do not match",
                                Toast.LENGTH_SHORT).show();
                    } else {
                        // TODO: update via your auth backend
                        Toast.makeText(this, "Password changed",
                                Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showDeleteAccountDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Delete Account")
                .setMessage("Are you sure? This cannot be undone.")
                .setPositiveButton("Delete", (dlg, which) -> {
                    // TODO: call your deletion API, clear data
                    Toast.makeText(this, "Account deleted",
                            Toast.LENGTH_SHORT).show();
                    finishAffinity();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
