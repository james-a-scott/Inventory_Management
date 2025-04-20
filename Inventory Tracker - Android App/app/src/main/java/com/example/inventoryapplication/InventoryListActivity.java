package com.example.inventoryapplication;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.SearchView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.android.volley.Request;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * InventoryListActivity is responsible for displaying and managing inventory items.
 * It supports functionalities like searching, pagination, filtering,
 * and role-based access control (RBAC) for adding/editing items.
 * Features include:
 * - JWT Authentication token handling for secure API access
 * - Role-based UI element display (Admins can add items)
 * - Pagination with dynamic page size
 * - Real-time search and filter functionality
 * This activity is the main screen for interacting with inventory data.
 */
public class InventoryListActivity extends AppCompatActivity {

    // Current page size and index for paginated item display
    private int pageSize = 10;
    private int currentPage = 1;

    // UI components for pagination and empty list display
    private TextView emptyListView;
    private Button prevButton;
    private Button nextButton;

    // Token for user authentication
    private String authToken;

    // Item list containers
    private final List<Item> mItemList = new ArrayList<>();     // Full dataset from API
    private final List<Item> filteredList = new ArrayList<>();  // Filtered by search query
    private final List<Item> pageList = new ArrayList<>();      // Current page only

    private ItemAdapter adapter;

    /**
     * Initializes the activity, sets up the UI components, and handles the setup of
     * authentication, data retrieval, pagination, search, and filtering.
     * @param savedInstanceState A Bundle containing the state of the activity if it was previously
     *                           created. Otherwise, it is null.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inventory_list);

        try {
            // Load the auth token from persistent storage (SharedPreferences)
            SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
            authToken = prefs.getString("auth_token", "");

            // Decode token to extract the user's role (e.g., Admin or User)
            String userRole = getUserRoleFromToken(authToken);

            // Setup an activity launcher to refresh the item list when returning from editing
            ActivityResultLauncher<Intent> editItemLauncher = registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        if (result.getResultCode() == RESULT_OK) {
                            fetchInventoryItems(); // Refresh on successful edit
                        }
                    }
            );

            // Link XML views to Java objects
            RecyclerView itemListView = findViewById(R.id.itemListView);
            emptyListView = findViewById(R.id.emptyListView);
            SearchView searchView = findViewById(R.id.searchView);
            Spinner pageSizeSpinner = findViewById(R.id.pageSizeSpinner);
            Button refreshButton = findViewById(R.id.refreshButton);
            Button addButton = findViewById(R.id.addButton);
            prevButton = findViewById(R.id.prevButton);
            nextButton = findViewById(R.id.nextButton);

            // If user is not an Admin, restrict access to the Add button (RBAC)
            if (!"Admin".equalsIgnoreCase(userRole)) {
                addButton.setVisibility(View.GONE);
            }

            // Setup the RecyclerView to show inventory items
            itemListView.setLayoutManager(new LinearLayoutManager(this));
            adapter = new ItemAdapter(pageList, this, authToken, userRole, editItemLauncher);
            itemListView.setAdapter(adapter);

            // Spinner lets user choose page size (e.g., 10, 50, 100)
            pageSizeSpinner.setSelection(1); // Default to 10
            pageSizeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    try {
                        String selectedValue = (String) parent.getItemAtPosition(position);
                        pageSize = Integer.parseInt(selectedValue); // Convert selection to int
                        updatePage(); // Refresh pagination
                    } catch (NumberFormatException e) {
                        Log.e("InventoryListActivity", "Invalid page size selected", e);
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {}
            });

            // Pagination controls
            prevButton.setOnClickListener(v -> {
                if (currentPage > 1) {
                    currentPage--;
                    updatePage();
                }
            });

            nextButton.setOnClickListener(v -> {
                if (currentPage * pageSize < filteredList.size()) {
                    currentPage++;
                    updatePage();
                }
            });

            prevButton.setEnabled(false);
            nextButton.setEnabled(false);

            // Manual refresh and new item entry handlers
            refreshButton.setOnClickListener(v -> fetchInventoryItems());
            addButton.setOnClickListener(v -> startActivity(new Intent(this, AddItemActivity.class)));

            // Listen for real-time search queries
            searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String query) { return false; }

                @Override
                public boolean onQueryTextChange(String newText) {
                    filterItems(newText); // Apply filtering based on query
                    return true;
                }
            });

            // Load inventory list from backend
            fetchInventoryItems();

        } catch (Exception e) {
            Log.e("InventoryListActivity", "Unexpected error during initialization", e);
            Toast.makeText(this, "App initialization failed", Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Refreshes the inventory list every time the activity regains focus.
     */
    @Override
    protected void onResume() {
        super.onResume();
        fetchInventoryItems(); // Reload data every time the screen regains focus
    }

    /**
     * Creates the options menu with actions such as notifications and logout.
     * @param menu The menu that will be displayed.
     * @return true if the menu was created successfully.
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate top-right options (Notifications & Logout)
        getMenuInflater().inflate(R.menu.appbar_menu, menu);
        return true;
    }

    /**
     * Handles item selection from the options menu, including actions like logout.
     * @param item The selected menu item.
     * @return true if the item selection is handled successfully.
     */
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        try {
            int id = item.getItemId();

            if (id == R.id.action_toggle_notifications) {
                // Launch SMS notifications settings
                startActivity(new Intent(this, SmsNotificationsActivity.class));
                return true;
            } else if (id == R.id.action_logout) {
                // Clear login token and return to login screen
                SharedPreferences.Editor editor = getSharedPreferences("user_prefs", MODE_PRIVATE).edit();
                editor.remove("auth_token");
                editor.apply();

                Intent intent = new Intent(this, ActivityLogin.class);
                startActivity(intent);
                finish(); // Prevent back navigation
                return true;
            }
        } catch (Exception e) {
            Log.e("InventoryListActivity", "Menu action failed", e);
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Requests the inventory list from the backend API.
     * Parses the JSON response and updates the internal data list.
     */
    private void fetchInventoryItems() {
        String url = "http://192.168.86.33:3000/api/items";

        Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", "Bearer " + authToken);

        JsonArrayRequest req = new JsonArrayRequest(
                Request.Method.GET, url, null,
                response -> {
                    try {
                        mItemList.clear(); // Clear old data
                        for (int i = 0; i < response.length(); i++) {
                            JSONObject o = response.getJSONObject(i);
                            Item item = new Item("", "", "", 0);
                            item.setId(o.getString("_id"));
                            item.setCode(o.getString("code"));
                            item.setName(o.getString("name"));
                            item.setQuantity(o.getInt("quantity"));
                            mItemList.add(item);
                        }
                        filterItems(""); // Trigger display of all items
                    } catch (JSONException e) {
                        Log.e("InventoryListActivity", "Failed to parse API response", e);
                        Toast.makeText(this, "Data parsing error", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    Log.e("InventoryListActivity", "API request failed", error);
                    Toast.makeText(this, "Unable to load items", Toast.LENGTH_SHORT).show();
                }
        ) {
            @Override
            public Map<String, String> getHeaders() {
                return headers; // Attach Bearer token for authorization
            }
        };

        Volley.newRequestQueue(this).add(req); // Execute request
    }

    /**
     * Filters the inventory items based on the user's search query.
     * @param query The search query to filter inventory items by name.
     */
    private void filterItems(String query) {
        try {
            filteredList.clear();
            for (Item item : mItemList) {
                if (item.getName().toLowerCase().contains(query.toLowerCase())) {
                    filteredList.add(item);
                }
            }
            currentPage = 1; // Reset to first page on new search
            updatePage();
        } catch (Exception e) {
            Log.e("InventoryListActivity", "Error filtering items", e);
        }
    }

    /**
     * Updates the displayed list based on the current page.
     * Handles enabling/disabling pagination buttons.
     */
    private void updatePage() {
        try {
            pageList.clear();

            int startIndex = (currentPage - 1) * pageSize;
            int endIndex = Math.min(startIndex + pageSize, filteredList.size());

            for (int i = startIndex; i < endIndex; i++) {
                pageList.add(filteredList.get(i));
            }

            prevButton.setEnabled(currentPage > 1);
            nextButton.setEnabled(currentPage * pageSize < filteredList.size());

            emptyListView.setVisibility(filteredList.isEmpty() ? View.VISIBLE : View.GONE);
            adapter.notifyDataSetChanged(); // Notify UI to refresh
        } catch (Exception e) {
            Log.e("InventoryListActivity", "Pagination logic error", e);
        }
    }

    /**
     * Extracts the user's role from the JWT payload for role-based access control (RBAC).
     * @param token The JWT token used for authentication.
     * @return The user's role extracted from the JWT token, defaulting to "User".
     */
    private String getUserRoleFromToken(String token) {
        try {
            String[] parts = token.split("\\.");
            if (parts.length >= 2) {
                String payload = parts[1];

                byte[] decodedBytes = android.util.Base64.decode(payload, android.util.Base64.URL_SAFE);
                String decodedPayload = new String(decodedBytes, java.nio.charset.StandardCharsets.UTF_8);

                JSONObject jsonObject = new JSONObject(decodedPayload);
                return jsonObject.optString("role", "User"); // Default fallback
            }
        } catch (Exception e) {
            Log.e("InventoryListActivity", "Failed to decode JWT", e);
        }

        return "User";
    }
}
