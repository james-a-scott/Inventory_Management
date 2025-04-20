package com.example.inventoryapplication;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
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
 * AddItemActivity is responsible for allowing the user to add a new item to the inventory.
 * This activity provides input fields for the item name, code, and quantity, and handles
 * communication with the backend API to store the new item.
 * The activity also provides features to increment and decrement the item quantity with
 * validation in place to prevent invalid entries.
 */
public class AddItemActivity extends AppCompatActivity {

    /** UI components for the item name, code, and quantity input */
    private EditText addItemName, addItemCode, addQuantity;

    /** The authorization token used to authenticate the API request */
    private String authToken;

    /**
     * This method is called when the activity is first created.
     * It initializes the user interface components and sets up the event listeners for
     * buttons to interact with the user (e.g., increment/decrement quantity, save item).
     * @param savedInstanceState The saved instance state used to restore the activity's state if necessary.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_item);  // Ensure this matches your XML layout file

        try {
            // Initialize input fields and buttons from the XML layout
            addItemName = findViewById(R.id.addItemName);
            addItemCode = findViewById(R.id.addItemCode);
            addQuantity = findViewById(R.id.addQuantity_add);  // From XML
            ImageButton increaseQuantityBtn = findViewById(R.id.increaseQuantityBtn_add);  // From XML
            ImageButton decreaseQuantityBtn = findViewById(R.id.decreaseQuantityBtn_add);  // From XML

            // Retrieve the stored authentication token from SharedPreferences
            SharedPreferences sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE);
            authToken = sharedPreferences.getString("auth_token", "");

            // Set listeners to handle quantity increment and decrement actions
            increaseQuantityBtn.setOnClickListener(this::incrementQuantity);
            decreaseQuantityBtn.setOnClickListener(this::decrementQuantity);

            // Set listener for the save item button to trigger the API request
            Button saveItemButton = findViewById(R.id.saveItem);  // From XML
            saveItemButton.setOnClickListener(this::handleSaveItem);
        } catch (Exception e) {
            // Catch any initialization errors and log them
            Log.e("AddItemActivity", "Error initializing views", e);
            Toast.makeText(this, "Failed to initialize the activity", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Increments the quantity of the item by 1.
     * This method updates the quantity input field and ensures the quantity is a valid number.
     * @param view The view (button) that was clicked to trigger this method.
     */
    public void incrementQuantity(View view) {
        try {
            int quantity = getCurrentQuantity();
            addQuantity.setText(String.valueOf(quantity + 1));
        } catch (Exception e) {
            // Catch any errors during quantity increment
            Log.e("AddItemActivity", "Error incrementing quantity", e);
            Toast.makeText(this, "Failed to increment quantity", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Decrements the quantity of the item by 1, ensuring the quantity cannot go below 0.
     * If the quantity is already 0, the user is informed that the quantity cannot be negative.
     * @param view The view (button) that was clicked to trigger this method.
     */
    public void decrementQuantity(View view) {
        try {
            int quantity = getCurrentQuantity();
            if (quantity > 0) {
                addQuantity.setText(String.valueOf(quantity - 1));
            } else {
                Toast.makeText(this, "Quantity cannot be less than 0", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            // Catch any errors during quantity decrement
            Log.e("AddItemActivity", "Error decrementing quantity", e);
            Toast.makeText(this, "Failed to decrement quantity", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Retrieves the current quantity value from the quantity input field.
     * If the input is empty or invalid, the method defaults to returning 0.
     * @return The current quantity as an integer (defaults to 0 if input is invalid).
     */
    private int getCurrentQuantity() {
        try {
            String quantityText = addQuantity.getText().toString();
            if (quantityText.isEmpty()) {
                return 0;
            }
            return Integer.parseInt(quantityText);
        } catch (NumberFormatException e) {
            // Handle invalid number input (e.g., empty string, non-numeric value)
            Log.e("AddItemActivity", "Error parsing quantity", e);
            Toast.makeText(this, "Invalid quantity input", Toast.LENGTH_SHORT).show();
            return 0;  // Return 0 if the input is invalid
        }
    }

    /**
     * Handles saving the item details and sending them to the backend API.
     * This method validates the input fields, creates a JSON object, and makes a POST request
     * to the API to add the item to the inventory. If the item is successfully added, the activity
     * returns to the previous screen.
     * @param view The view (button) that was clicked to trigger this method.
     */
    public void handleSaveItem(View view) {
        try {
            String name = addItemName.getText().toString().trim();
            String code = addItemCode.getText().toString().trim();
            int quantity = getCurrentQuantity();

            // Ensure both item name and code are entered
            if (name.isEmpty() || code.isEmpty()) {
                Toast.makeText(this, "Please enter both item name and code", Toast.LENGTH_SHORT).show();
                return;
            }

            // API URL to create a new item
            String url = "http://192.168.86.33:3000/api/items";

            // Prepare the JSON object with item details to send in the POST request
            JSONObject itemData = new JSONObject();
            try {
                itemData.put("name", name);
                itemData.put("code", code);
                itemData.put("quantity", quantity);
            } catch (JSONException e) {
                // Catch JSON-related exceptions (e.g., invalid JSON format)
                Log.e("AddItemActivity", "JSON Error: " + e.getMessage());
                Toast.makeText(this, "Failed to create item data", Toast.LENGTH_SHORT).show();
                return;
            }

            // Create a POST request using Volley to send item data to the backend API
            JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, itemData,
                    response -> {
                        try {
                            // Notify the user if the item is successfully added
                            Toast.makeText(this, "Item added successfully!", Toast.LENGTH_SHORT).show();
                            // Return to the previous screen
                            Intent resultIntent = new Intent();
                            setResult(RESULT_OK, resultIntent);
                            finish();
                        } catch (Exception e) {
                            // Handle any errors during the response handling
                            Log.e("AddItemActivity", "Error processing successful response", e);
                            Toast.makeText(this, "Failed to process server response", Toast.LENGTH_SHORT).show();
                        }
                    },
                    error -> {
                        // Handle any errors during the API request
                        Log.e("AddItemActivity", "Error: " + error.toString());
                        Toast.makeText(this, "Failed to add item", Toast.LENGTH_SHORT).show();
                    }
            ) {
                /**
                 * Adds authorization headers to the request.
                 * @return The headers to be included in the request.
                 */
                @Override
                public Map<String, String> getHeaders() {
                    Map<String, String> headers = new HashMap<>();
                    headers.put("Authorization", "Bearer " + authToken);  // Add the auth token to the headers
                    headers.put("Content-Type", "application/json");  // Set content type to JSON
                    return headers;
                }
            };

            // Add the request to the Volley request queue for execution
            Volley.newRequestQueue(this).add(request);

        } catch (Exception e) {
            // Catch any errors during the item-saving process
            Log.e("AddItemActivity", "Error in handleSaveItem", e);
            Toast.makeText(this, "An error occurred while saving the item", Toast.LENGTH_SHORT).show();
        }
    }
}
