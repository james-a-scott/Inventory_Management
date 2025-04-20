package com.example.inventoryapplication;

import android.content.Context;
import android.content.SharedPreferences;
import android.telephony.SmsManager;
import android.util.Log;
import java.io.Serializable;

public class Item implements Serializable {
    private final long mId; // Unique identifier for the item
    private String mName; // Name of the item
    private int mQuantity; // Quantity of the item

    // Constructor for initializing an item with an ID
    public Item(long id, String name, int quantity) {
        this.mId = id;
        this.mName = name;
        this.mQuantity = quantity;
    }

    // Returns the item's ID
    public long getId() {
        return mId;
    }

    // Returns the item's name
    public String getName() {
        return mName;
    }

    // Returns the item's quantity
    public int getQuantity() {
        return mQuantity;
    }

    // Sets a new name for the item
    public void setName(String name) {
        this.mName = name;
    }

    // Sets a new quantity for the item
    public void setQuantity(int quantity) {
        this.mQuantity = quantity;
    }

    // Decrements the quantity of the item and sends an SMS notification if it reaches zero
    public void decrementQuantity(Context context) {
        if (mQuantity > 0) {
            this.mQuantity--; // Reduce quantity
        }

        // Check if the quantity has reached zero
        if (mQuantity == 0) {
            sendSmsNotification(context); // Trigger SMS notification
        }
    }

    // Increments the quantity of the item
    public void incrementQuantity() {
        this.mQuantity++; // Increase quantity
    }

    // Sends an SMS notification when the item quantity reaches zero
    private void sendSmsNotification(Context context) {
        // Load user preference for SMS notifications
        SharedPreferences preferences = context.getSharedPreferences(SmsNotificationsActivity.PREFS_NAME, Context.MODE_PRIVATE);
        boolean receiveNotifications = preferences.getBoolean(SmsNotificationsActivity.KEY_RECEIVE_NOTIFICATIONS, false);

        // Exit if notifications are disabled
        if (!receiveNotifications) {
            Log.d("Item", "Notifications are turned off. Not sending SMS.");
            return;
        }

        String phoneNumber = "15551234567"; // Placeholder for the actual phone number
        String message = "The item '" + mName + "' has reached a quantity of 0."; // Notification message

        // Obtain the default SmsManager instance
        SmsManager smsManager = SmsManager.getDefault(); // Note: Deprecated in API 31

        // Attempt to send the SMS
        try {
            smsManager.sendTextMessage(phoneNumber, null, message, null, null);
            Log.d("Item", "SMS sent: " + message);
        } catch (Exception e) {
            Log.e("Item", "Failed to send SMS: " + e.getMessage()); // Log any errors during SMS sending
        }
    }
}
