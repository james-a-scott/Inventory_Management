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

/**
 * Activity that manages user preference for receiving SMS notifications.
 * It uses a toggle switch UI to enable/disable notifications and handles
 * runtime SMS permissions using the Android permissions framework.
 * User preferences are persisted with SharedPreferences.
 */
public class SmsNotificationsActivity extends AppCompatActivity {

    /** Tag used for log messages specific to this activity. */
    private static final String TAG = "SmsNotificationsActivity";

    /** Request code for requesting the SEND_SMS permission. */
    private static final int REQUEST_SEND_SMS_CODE = 0;

    /** UI element for toggling SMS notifications. */
    private SwitchMaterial notificationsToggle;

    /** Flag tracking whether the user has enabled SMS notifications. */
    private boolean receiveNotifications = false;

    // SharedPreferences keys
    public static final String PREFS_NAME = "UserPrefs";
    public static final String KEY_RECEIVE_NOTIFICATIONS = "receive_notifications";


    /**
     * Lifecycle method invoked when the activity is created.
     * Sets up UI, loads stored preferences, and attaches interaction listeners.
     * @param savedInstanceState Bundle containing previously saved state (if any)
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            // Load and set the layout for this activity
            setContentView(R.layout.activity_sms_notifications);

            // Bind the toggle switch UI component
            notificationsToggle = findViewById(R.id.notificationsToggle);

            // Retrieve saved user preference
            loadNotificationPreference();

            // Reflect the saved preference on the toggle state
            notificationsToggle.setChecked(receiveNotifications);

            // Handle changes in the toggle switch state
            notificationsToggle.setOnCheckedChangeListener((buttonView, isChecked) -> {
                try {
                    if (isChecked) {
                        // If toggled ON and permission is granted, update preference
                        if (hasPermissions()) {
                            receiveNotifications = true;
                            saveNotificationPreference();
                        } else {
                            // Request permission if not granted
                            ActivityCompat.requestPermissions(
                                    this,
                                    new String[]{Manifest.permission.SEND_SMS},
                                    REQUEST_SEND_SMS_CODE
                            );
                        }
                    } else {
                        // Toggle OFF - disable notifications
                        receiveNotifications = false;
                        saveNotificationPreference();
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error handling toggle change: " + e.getMessage());
                }
            });

            // Ensure permission check happens at startup for readiness
            hasPermissions();

        } catch (Exception e) {
            Log.e(TAG, "Error during onCreate: " + e.getMessage());
        }
    }

    /**
     * Loads user notification preference from local storage.
     * Defaults to false if preference hasn't been set previously.
     */
    private void loadNotificationPreference() {
        try {
            SharedPreferences preferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
            receiveNotifications = preferences.getBoolean(KEY_RECEIVE_NOTIFICATIONS, false);
        } catch (Exception e) {
            Log.e(TAG, "Failed to load preference: " + e.getMessage());
        }
    }

    /**
     * Saves the current notification preference to local storage.
     * Uses apply() for asynchronous and non-blocking preference saving.
     */
    private void saveNotificationPreference() {
        try {
            SharedPreferences preferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
            SharedPreferences.Editor editor = preferences.edit();
            editor.putBoolean(KEY_RECEIVE_NOTIFICATIONS, receiveNotifications);
            editor.apply(); // Non-blocking save
        } catch (Exception e) {
            Log.e(TAG, "Failed to save preference: " + e.getMessage());
        }
    }

    /**
     * Verifies if the SEND_SMS permission has been granted by the user.
     * @return true if permission is granted; false otherwise
     */
    private boolean hasPermissions() {
        try {
            boolean granted = ContextCompat.checkSelfPermission(
                    this, Manifest.permission.SEND_SMS
            ) == PackageManager.PERMISSION_GRANTED;

            if (!granted) {
                Log.d(TAG, "SMS permission not granted.");
            }

            return granted;
        } catch (Exception e) {
            Log.e(TAG, "Permission check failed: " + e.getMessage());
            return false;
        }
    }

    /**
     * Handles the result of the SEND_SMS permission request.
     * Updates preference and UI state based on user response.
     * @param requestCode   Identifier for the permission request
     * @param permissions   Array of requested permissions
     * @param grantResults  Result codes corresponding to requested permissions
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        try {
            if (requestCode == REQUEST_SEND_SMS_CODE) {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d(TAG, "Permission granted by user.");
                    receiveNotifications = true;
                } else {
                    Log.d(TAG, "Permission denied by user.");
                    receiveNotifications = false;
                }

                // Update and persist new state regardless of grant result
                saveNotificationPreference();
                notificationsToggle.setChecked(receiveNotifications);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error processing permission result: " + e.getMessage());
        }
    }
}
