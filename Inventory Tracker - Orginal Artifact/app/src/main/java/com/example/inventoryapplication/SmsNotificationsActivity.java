package com.example.inventoryapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;

import com.google.android.material.switchmaterial.SwitchMaterial;

public class SmsNotificationsActivity extends AppCompatActivity {

    // Logcat tag for debugging
    private static final String TAG = "SmsNotificationsActivity";

    // Request code for SMS permission
    private final int REQUEST_SEND_SMS_CODE = 0;

    private SwitchMaterial notificationsToggle;
    private boolean receiveNotifications = false;

    // SharedPreferences keys
    public static final String PREFS_NAME = "UserPrefs";
    public static final String KEY_RECEIVE_NOTIFICATIONS = "receive_notifications";
    public static final String KEY_SHOW_PROMPT_AGAIN = "show_prompt_again";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sms_notifications);

        notificationsToggle = findViewById(R.id.notificationsToggle);

        // Load current notification preference
        loadNotificationPreference();

        // Set initial state of the toggle switch
        notificationsToggle.setChecked(receiveNotifications);

        // Handle changes to the toggle switch
        notificationsToggle.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                // Check for SMS permission
                if (hasPermissions()) {
                    receiveNotifications = true;
                    saveNotificationPreference();
                } else {
                    // Request SMS permission if not granted
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.SEND_SMS}, REQUEST_SEND_SMS_CODE);
                }
            } else {
                receiveNotifications = false;
                saveNotificationPreference();
            }
        });

        // Check permissions at startup
        hasPermissions();
    }

    // Load notification preference from SharedPreferences
    private void loadNotificationPreference() {
        SharedPreferences preferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        receiveNotifications = preferences.getBoolean(KEY_RECEIVE_NOTIFICATIONS, false); // Default is false
    }

    // Save notification preference to SharedPreferences
    private void saveNotificationPreference() {
        SharedPreferences preferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean(KEY_RECEIVE_NOTIFICATIONS, receiveNotifications);
        editor.apply();
    }

    // Check if SMS permission is granted
    private boolean hasPermissions() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED;
    }

    // Handle the result of the permission request
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_SEND_SMS_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "Permission granted");
                receiveNotifications = true;
            } else {
                Log.d(TAG, "Permission denied");
                receiveNotifications = false;
            }

            // Update preference and toggle state
            saveNotificationPreference();
            notificationsToggle.setChecked(receiveNotifications);
        }
    }
}
