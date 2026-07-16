package com.zybooks.inventorytracking;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import androidx.appcompat.app.AlertDialog;

import android.graphics.Color;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;


import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.color.MaterialColors;

// Handles SMS permission request and phone number setup for low inventory alerts
public class SmsPermissionActivity extends BaseActivity {
    private ActivityResultLauncher<String> mPermissionLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sms_permission);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.rootLayout), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Button grantButton = findViewById(R.id.grant_sms_permission_button);
        Button declineButton = findViewById(R.id.decline_sms_button);

        // If permission is granted, prompt for phone number, otherwise open dashboard
        mPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                isGranted -> {
                    if (isGranted) {
                        showPermissionDialog();
                    } else {
                        goToDashboard();
                    }

                }
        );

        grantButton.setOnClickListener(v -> {
            mPermissionLauncher.launch(Manifest.permission.SEND_SMS);
        });

        declineButton.setOnClickListener(v -> {
            goToDashboard();
        });
    }

    // Prompts the user to enter a phone number and saves it to shared preferences for SMS alerts
    private void showPermissionDialog() {
        final EditText input = new EditText(this);
        input.setHint("Enter your phone number");
        input.setInputType(InputType.TYPE_CLASS_PHONE);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("SMS Alerts")
                .setMessage("Enter your phone number to receive low inventory alerts.")
                .setView(input)
                .setPositiveButton("Save", (d, which) -> {
                    String phoneNumber = input.getText().toString().trim();
                    if (!phoneNumber.isEmpty()) {
                        SharedPreferences prefs = getSharedPreferences("InventoryPrefs", MODE_PRIVATE);
                        prefs.edit().putString("alert_phone_number", phoneNumber).apply();
                        Log.d("SMS", "Phone number saved: " + phoneNumber);
                    }
                    goToDashboard();
                })
                .setNegativeButton("Skip", (d, which) -> goToDashboard())
                .create();

        dialog.show();

        // Set button colors explicitly for contrast
        int color = MaterialColors.getColor(this, com.google.android.material.R.attr.colorSecondary, Color.BLACK);
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(color);
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(color);
    }

    // Saves SMS prompt state and navigates to dashboard
    private void goToDashboard() {
        SharedPreferences prefs = getSharedPreferences("InventoryPrefs", MODE_PRIVATE);
        prefs.edit().putBoolean("sms_prompt_shown", true).apply();
        Intent intent = new Intent(this, DashboardActivity.class);
        startActivity(intent);
        finish();
    }

}
