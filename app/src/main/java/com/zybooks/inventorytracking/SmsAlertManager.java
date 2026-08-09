package com.zybooks.inventorytracking;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.telephony.SmsManager;
import android.util.Log;

import androidx.core.content.ContextCompat;

/* Handles SMS inventory alerts, checks permissions and preferences, sends SMS if permissions enabled
 * Returns error if permissions not granted or in case of failure */
public class SmsAlertManager {

    private static final String PREFS_NAME = "InventoryPrefs";
    private static final String PREF_ALERTS_ENABLED = "sms_alerts_enabled";
    private static final String PREF_PHONE_NUMBER = "alert_phone_number";

    private final Context mContext;

    public SmsAlertManager(Context context) {
        mContext = context.getApplicationContext();
    }

    public void sendLowInventoryAlert(String itemName) {
        SharedPreferences prefs = mContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        boolean alertsEnabled = prefs.getBoolean(PREF_ALERTS_ENABLED, true);
        if (!alertsEnabled) {
            Log.d("SMS", "SMS alerts disabled by user");
            return;
        }

        if (ContextCompat.checkSelfPermission(mContext, Manifest.permission.SEND_SMS)
                != PackageManager.PERMISSION_GRANTED) {
            Log.d("SMS", "SMS permission not granted, skipping alert");
            return;
        }

        String phoneNumber = prefs.getString(PREF_PHONE_NUMBER, null);
        if (phoneNumber == null || phoneNumber.isEmpty()) {
            Log.d("SMS", "No phone number saved, skipping alert");
            return;
        }

        String message = "Low Inventory Alert: " + itemName +
                " is out of stock. Please consider replenishing.";

        try {
            SmsManager smsManager = mContext.getSystemService(SmsManager.class);
            smsManager.sendTextMessage(phoneNumber, null, message, null, null);
            Log.d("SMS", "SMS sent to " + phoneNumber);
        } catch (Exception e) {
            Log.e("SMS", "Failed to send SMS: " + e.getMessage());
        }
    }
}
