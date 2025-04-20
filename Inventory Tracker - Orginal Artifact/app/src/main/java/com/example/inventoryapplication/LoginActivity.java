package com.example.inventoryapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.security.MessageDigest;

public class LoginActivity extends AppCompatActivity {

    // Instance of the database
    InventoryDatabase inventoryDatabase;

    // Cached view elements
    EditText usernameInput;
    EditText passwordInput;

    Button loginBtn;
    Button registerBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Get the singleton instance of the app database
        inventoryDatabase = InventoryDatabase.getInstance(this);

        // Cache the view elements for efficient access
        usernameInput = findViewById(R.id.usernameInput);
        passwordInput = findViewById(R.id.passwordInput);
        loginBtn = findViewById(R.id.loginBtn);
        registerBtn = findViewById(R.id.registerBtn);

        // Disable buttons until valid input is provided
        loginBtn.setEnabled(false);
        registerBtn.setEnabled(false);

        // Add text change listeners to enable buttons when inputs are filled
        usernameInput.addTextChangedListener(textWatcher);
        passwordInput.addTextChangedListener(textWatcher);
    }

    // TextWatcher to monitor input changes
    private TextWatcher textWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            // No action required before text changes
        }

        @Override
        public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
            // Enable buttons if username and password fields are filled
            boolean fieldsAreEmpty = getUsername().isEmpty() || getPassword().isEmpty();
            loginBtn.setEnabled(!fieldsAreEmpty);
            registerBtn.setEnabled(!fieldsAreEmpty);
        }

        @Override
        public void afterTextChanged(Editable s) {
            // No action required after text changes
        }
    };

    // Attempt to log the user in
    public void login(View view) {
        // Validate credentials before proceeding
        if (!validCredentials()) {
            showError(view.getContext().getResources().getString(R.string.invalid_login));
            return;
        }

        try {
            // Check user credentials in the database
            boolean isLoggedIn = inventoryDatabase.checkUser(getUsername(), hash(getPassword()));

            // Navigate to inventory list if login is successful
            if (isLoggedIn) {
                handleLoggedInUser();
            } else {
                showError(view.getContext().getResources().getString(R.string.invalid_login));
            }
        } catch (Exception e) {
            showError(view.getContext().getResources().getString(R.string.invalid_login));
        }
    }

    // Register a new user
    public void register(View view) {
        // Ensure provided credentials are valid (not empty)
        if (!validCredentials()) {
            showError(view.getContext().getResources().getString(R.string.registration_error));
            return; // Exit if credentials are invalid
        }

        try {
            // Attempt to create a new user in the database
            boolean userCreated = inventoryDatabase.addUser(getUsername(), hash(getPassword()));

            // Navigate to inventory list if registration is successful
            if (userCreated) {
                handleLoggedInUser();
            } else {
                showError(view.getContext().getResources().getString(R.string.registration_error));
            }
        } catch (Exception e) {
            showError(view.getContext().getResources().getString(R.string.registration_error));
        }
    }

    // Navigate to the inventory list screen
    private void handleLoggedInUser() {
        Intent intent = new Intent(getApplicationContext(), InventoryListActivity.class);
        startActivity(intent);
    }

    // Validate that credentials are not empty
    private boolean validCredentials() {
        return !getUsername().isEmpty() && !getPassword().isEmpty();
    }

    // Get the username input text
    private String getUsername() {
        Editable username = usernameInput.getText();
        return username != null ? username.toString().trim().toLowerCase() : "";
    }

    // Get the password input text
    private String getPassword() {
        Editable password = passwordInput.getText();
        return password != null ? password.toString().trim() : "";
    }

    // Hash the given password string using MD5
    private String hash(String password) throws Exception {
        MessageDigest md = MessageDigest.getInstance("MD5");
        md.update(password.getBytes());
        byte[] digest = md.digest();
        StringBuffer sb = new StringBuffer();
        for (byte b : digest) {
            sb.append(String.format("%02x", b & 0xff));
        }
        return sb.toString();
    }

    // Helper function to display a Toast error message
    private void showError(String errorMessage) {
        Toast toast = Toast.makeText(getApplicationContext(), errorMessage, Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER, 0, -200);
        toast.show();
    }
}
