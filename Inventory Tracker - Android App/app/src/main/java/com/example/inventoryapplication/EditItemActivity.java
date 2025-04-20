package com.example.inventoryapplication;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.HashMap;
import java.util.Map;

/**
 * Activity class for editing an inventory item.
 * This activity allows users to edit the name and quantity of an item in the inventory.
 * It includes functionality to increment or decrement the item quantity and submit the changes
 * to the backend API via a PUT request. The activity also provides error handling and ensures
 * the input data is valid before saving.
 */
public class EditItemActivity extends AppCompatActivity {

    private EditText editItemName, editQuantity;
    private String authToken;  // Authentication token used for API request authorization
    private String itemCode;   // Unique code of the item being edited

    /**
     * Called when the activity is created.
     * This method initializes the views, retrieves the authentication token,
     * and sets the item data from the Intent. It also sets up button listeners
     * for quantity increment and decrement actions.
     * @param savedInstanceState A bundle containing the activity's previously saved state.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_item);

        try {
            // Initialize the UI components for item name and quantity
            editItemName = findViewById(R.id.editItemName);
            editQuantity = findViewById(R.id.editQuantity_edit);
            ImageButton increaseQuantityBtn = findViewById(R.id.increaseQuantityBtn_edit);
            ImageButton decreaseQuantityBtn = findViewById(R.id.decreaseQuantityBtn_edit);

            // Retrieve authentication token from SharedPreferences for authorization in API requests
            SharedPreferences sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE);
            authToken = sharedPreferences.getString("auth_token", "");

            // Extract item data from the Intent
            Intent intent = getIntent();
            itemCode = intent.getStringExtra("ITEM_CODE");
            String itemName = intent.getStringExtra("ITEM_NAME");
            int itemQuantity = intent.getIntExtra("ITEM_QUANTITY", 0);

            // Debug logging to verify received item details
            Log.d("EditItemActivity", "Item Code: " + itemCode);
            Log.d("EditItemActivity", "Item Name: " + itemName);
            Log.d("EditItemActivity", "Item Quantity: " + itemQuantity);

            // Populate the views with the item data
            editItemName.setText(itemName);
            editQuantity.setText(String.valueOf(itemQuantity));

            // Set up event listeners for quantity increment and decrement buttons
            increaseQuantityBtn.setOnClickListener(this::incrementQuantity);
            decreaseQuantityBtn.setOnClickListener(this::decrementQuantity);

        } catch (Exception e) {
            // Catch any exceptions during view initialization or data retrieval
            Log.e("EditItemActivity", "Error initializing views or getting item data", e);
            Toast.makeText(this, "Error initializing the activity", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Increments the quantity of the item by 1 and updates the UI.
     * This method is invoked when the user presses the "Increase Quantity" button.
     * It retrieves the current quantity, increments it by 1, and updates the EditText field.
     * @param view The view that triggered the event (not used in this case).
     */
    public void incrementQuantity(View view) {
        try {
            // Get the current quantity from the EditText and increment it
            int quantity = getCurrentQuantity();
            editQuantity.setText(String.valueOf(quantity + 1));
        } catch (Exception e) {
            // Log any exceptions during the quantity increment process
            Log.e("EditItemActivity", "Error incrementing quantity", e);
            Toast.makeText(this, "Failed to increment quantity", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Decrements the quantity of the item by 1 and updates the UI.
     * This method is invoked when the user presses the "Decrease Quantity" button.
     * It retrieves the current quantity, decrements it by 1 (if greater than 0),
     * and updates the EditText field. If the quantity is 0, it shows an error message.
     * @param view The view that triggered the event (not used in this case).
     */
    public void decrementQuantity(View view) {
        try {
            // Get the current quantity from the EditText and decrement it
            int quantity = getCurrentQuantity();
            if (quantity > 0) {
                editQuantity.setText(String.valueOf(quantity - 1));
            } else {
                // If quantity is already 0, show a toast indicating it cannot go lower
                Toast.makeText(this, "Quantity cannot be less than 0", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            // Log any exceptions during the quantity decrement process
            Log.e("EditItemActivity", "Error decrementing quantity", e);
            Toast.makeText(this, "Failed to decrement quantity", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Retrieves the current quantity of the item from the EditText field.
     * This method is used to get the current input value from the user. If the input is empty,
     * it returns 0. If the input is non-empty, it parses the value as an integer.
     * @return The current quantity as an integer.
     */
    private int getCurrentQuantity() {
        try {
            // Get the quantity text from the EditText field
            String quantityText = editQuantity.getText().toString();
            if (quantityText.isEmpty()) {
                return 0;
            }
            // Parse the string to an integer and return the value
            return Integer.parseInt(quantityText);
        } catch (NumberFormatException e) {
            // Handle any invalid number format exceptions (e.g., user enters non-numeric text)
            Log.e("EditItemActivity", "Error parsing quantity", e);
            Toast.makeText(this, "Invalid quantity input", Toast.LENGTH_SHORT).show();
            return 0;  // Return 0 as a default if parsing fails
        }
    }

    /**
     * Handles saving the edited item data to the backend API.
     * This method is triggered when the user presses the "Save Item" button.
     * It validates the input, constructs a JSON object with the updated data,
     * and sends a PUT request to the backend API to update the item details.
     * @param view The view that triggered the event (not used in this case).
     */
    public void handleSaveItem(View view) {
        try {
            // Get the name and quantity from the user input
            String name = editItemName.getText().toString().trim();
            int quantity = getCurrentQuantity();

            // Check if the quantity is zero and send SMS if true
            if (getCurrentQuantity() == 0) {
                sendSmsNotification(this, editItemName.getText().toString());
            }

            // Validate input: name cannot be empty
            if (name.isEmpty()) {
                Toast.makeText(this, "Please enter an item name", Toast.LENGTH_SHORT).show();
                return;
            }

            // Define the URL for the PUT request with the item code
            String url = "http://192.168.86.33:3000/api/items/" + itemCode;

            // Create a JSON object to hold the updated item data
            JSONObject itemData = new JSONObject();
            try {
                itemData.put("name", name);
                itemData.put("quantity", quantity);
            } catch (JSONException e) {
                // Handle JSON creation errors (e.g., if the JSON object cannot be constructed)
                Log.e("EditItemActivity", "JSON Error: " + e.getMessage());
                Toast.makeText(this, "Failed to create update data", Toast.LENGTH_SHORT).show();
                return;
            }

            // Create a request to update the item via the API
            JsonObjectRequest request = new JsonObjectRequest(Request.Method.PUT, url, itemData,
                    response -> {
                        try {
                            // If the update is successful, notify the user and return the result
                            Toast.makeText(this, "Item updated successfully!", Toast.LENGTH_SHORT).show();
                            Intent resultIntent = new Intent();
                            resultIntent.putExtra("updated", true);
                            setResult(RESULT_OK, resultIntent);
                            finish();
                        } catch (Exception e) {
                            // Handle any errors that occur while processing the response
                            Log.e("EditItemActivity", "Error processing response", e);
                            Toast.makeText(this, "Failed to process server response", Toast.LENGTH_SHORT).show();
                        }
                    },
                    error -> {
                        // Log and handle network or server errors during the request
                        Log.e("EditItemActivity", "Error: " + error.toString());
                        Toast.makeText(this, "Failed to update item", Toast.LENGTH_SHORT).show();
                    }
            ) {
                @Override
                public Map<String, String> getHeaders() {
                    // Add necessary headers for authorization and content type
                    Map<String, String> headers = new HashMap<>();
                    headers.put("Authorization", "Bearer " + authToken);
                    headers.put("Content-Type", "application/json");
                    return headers;
                }
            };

            // Add the request to the Volley request queue for execution
            Volley.newRequestQueue(this).add(request);
        } catch (Exception e) {
            // Catch any exceptions during the save process (e.g., network issues, JSON formatting)
            Log.e("EditItemActivity", "Error in handleSaveItem", e);
            Toast.makeText(this, "An error occurred while saving the item", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Sends an SMS alert to a predefined number if the quantity reaches zero.
     * SMS notifications only fire if the user has enabled the option in preferences.
     * This method requires the {@code SEND_SMS} permission.
     * @param context Android context used to access SharedPreferences and SMS system service
     * @param itemName Name of the item that reached zero quantity
     */
    void sendSmsNotification(Context context, String itemName) {
        try {
            SharedPreferences preferences = context.getSharedPreferences(
                    SmsNotificationsActivity.PREFS_NAME, Context.MODE_PRIVATE);
            boolean receiveNotifications = preferences.getBoolean(
                    SmsNotificationsActivity.KEY_RECEIVE_NOTIFICATIONS, false);

            if (!receiveNotifications) {
                Log.d("ItemAdapter", "User has opted out of SMS notifications.");
                return;
            }

            String phoneNumber = "15551234567";  // TODO: Replace with dynamic recipient
            String message = "The item '" + itemName + "' has reached a quantity of 0.";

            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(phoneNumber, null, message, null, null);
            Log.d("ItemAdapter", "SMS sent: " + message);

        } catch (SecurityException se) {
            Log.e("ItemAdapter", "Missing SMS permission: " + se.getMessage(), se);
        } catch (NullPointerException ne) {
            Log.e("ItemAdapter", "SMS service not available: " + ne.getMessage(), ne);
        } catch (Exception e) {
            Log.e("ItemAdapter", "Error sending SMS: " + e.getMessage(), e);
        }
    }
}
