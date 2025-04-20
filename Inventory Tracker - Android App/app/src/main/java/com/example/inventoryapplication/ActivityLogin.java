package com.example.inventoryapplication;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * ActivityLogin is the login screen for the inventory application.
 * It handles the authentication process by sending a login request to the server with the user's credentials.
 * On successful login, it stores the authentication token and user role in SharedPreferences,
 * and navigates the user to the InventoryListActivity.
 */
public class ActivityLogin extends AppCompatActivity {

    /** Logging tag for debugging and error tracking */
    private static final String TAG = "Activity_Login";

    /** UI elements for capturing user input */
    private EditText usernameInput, passwordInput;

    /**
     * This method is called when the Activity is first created.
     * It initializes the UI components and sets up the login button's click listener.
     * @param savedInstanceState the saved instance state, which can be used to restore the Activity's previous state
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Initialize UI elements
        try {
            usernameInput = findViewById(R.id.usernameInput);
            passwordInput = findViewById(R.id.passwordInput);
            Button loginBtn = findViewById(R.id.loginBtn);

            // Set up the login button's click listener to trigger login method
            loginBtn.setOnClickListener(v -> login());
        } catch (Exception e) {
            Log.e(TAG, "Error initializing views", e);
            Toast.makeText(ActivityLogin.this, "Failed to initialize UI components", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * This method is responsible for handling the login process.
     * It validates the user input, creates a JSON request with login credentials,
     * sends the request to the server, and handles the server's response.
     */
    private void login() {
        // Retrieve and trim the email and password input from the user
        String email = usernameInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();

        // Validate that both fields are filled
        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(ActivityLogin.this, "Please fill in both fields", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create a JSON object to send as part of the login request
        JSONObject loginRequest = new JSONObject();
        try {
            loginRequest.put("email", email);
            loginRequest.put("password", password);
        } catch (JSONException e) {
            // Log error if there's an issue creating the JSON request
            Log.e(TAG, "Failed to create JSON for login request", e);
            Toast.makeText(ActivityLogin.this, "An error occurred while preparing the request.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Define the backend URL for the login request
        String url = "http://192.168.86.33:3000/api/login";

        // Create a JSON object request using the Volley library to send the login request
        JsonObjectRequest loginRequestObject = new JsonObjectRequest(Request.Method.POST, url, loginRequest,
                response -> {
                    try {
                        // Check if the response contains the authentication token
                        if (response.has("token")) {
                            String token = response.getString("token");

                            // Retrieve the user role, default to "User" if not provided
                            String role = response.has("role") ? response.getString("role") : "User";

                            // Store the authentication token and user role in SharedPreferences for future use
                            SharedPreferences sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE);
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.putString("auth_token", token);
                            editor.putString("user_role", role);
                            editor.apply();

                            // Show a success message to the user
                            Toast.makeText(ActivityLogin.this, "Login successful", Toast.LENGTH_SHORT).show();

                            // Navigate to the next screen (InventoryListActivity) and close the login activity
                            startActivity(new Intent(getApplicationContext(), InventoryListActivity.class));
                            finish(); // Prevent the user from going back to the login screen using the back button

                        } else {
                            // Log a warning if the response does not contain the authentication token
                            Log.w(TAG, "Login failed: No token in response");
                            Toast.makeText(ActivityLogin.this, "Login failed: No token received", Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        // Handle any JSON parsing exceptions that occur while processing the response
                        Log.e(TAG, "Error parsing login response", e);
                        Toast.makeText(ActivityLogin.this, "Login failed: Invalid server response", Toast.LENGTH_SHORT).show();
                    } catch (Exception e) {
                        // Handle any unexpected errors during the response handling
                        Log.e(TAG, "Unexpected error during login response handling", e);
                        Toast.makeText(ActivityLogin.this, "Unexpected error during login", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    // Handle errors from the Volley request
                    try {
                        Log.e(TAG, "Login request error", error);
                        Toast.makeText(ActivityLogin.this, "Login failed: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    } catch (Exception e) {
                        // Handle any unexpected errors during the error response handling
                        Log.e(TAG, "Unexpected error during login request", e);
                        Toast.makeText(ActivityLogin.this, "Unexpected error during login", Toast.LENGTH_SHORT).show();
                    }
                });

        // Add the login request to the Volley request queue for execution
        try {
            Volley.newRequestQueue(this).add(loginRequestObject);
        } catch (Exception e) {
            // Handle any errors that occur when adding the request to the Volley queue
            Log.e(TAG, "Error adding login request to Volley queue", e);
            Toast.makeText(ActivityLogin.this, "Error sending login request", Toast.LENGTH_SHORT).show();
        }
    }
}
