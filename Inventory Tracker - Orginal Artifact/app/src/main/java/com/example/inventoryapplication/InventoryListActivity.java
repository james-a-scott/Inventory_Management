package com.example.inventoryapplication;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class InventoryListActivity extends AppCompatActivity {

    // Logcat tag for logging messages
    private static final String TAG = "InventoryList";

    // List containing all inventory items
    private List<Item> mItemList;

    // Instance of the application database for inventory management
    InventoryDatabase inventoryDatabase;

    // UI elements for displaying the item list and empty state message
    RecyclerView itemListView;
    TextView emptyListView;

    // Map to associate menu items with tags for easier handling
    private final Map<MenuItem, String> menuTags = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inventory_list);

        // Initialize the database and load inventory items
        inventoryDatabase = InventoryDatabase.getInstance(getApplicationContext());
        mItemList = inventoryDatabase.getItems();

        // Set up RecyclerView with a linear layout and item dividers
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        itemListView = findViewById(R.id.itemListView);
        itemListView.setLayoutManager(layoutManager);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(itemListView.getContext(),
                layoutManager.getOrientation());
        itemListView.addItemDecoration(dividerItemDecoration);

        // Reference to the TextView for displaying an empty state message
        emptyListView = findViewById(R.id.emptyListView);

        // Create and set the adapter for the RecyclerView
        ItemAdapter adapter = new ItemAdapter(mItemList, this, inventoryDatabase);
        adapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onChanged() {
                super.onChanged();
                checkListIsEmpty(); // Check if the list is empty when data changes
            }
        });

        itemListView.setAdapter(adapter);

        // Check if the list is empty and update the UI accordingly
        checkListIsEmpty();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the options menu from XML resource
        getMenuInflater().inflate(R.menu.appbar_menu, menu);

        // Associate menu items with their corresponding tags
        menuTags.put(menu.findItem(R.id.action_add_item), "add_item");
        menuTags.put(menu.findItem(R.id.action_toggle_notifications), "toggle_notifications");
        menuTags.put(menu.findItem(R.id.action_logout), "logout");

        // Create a common action listener for menu item clicks
        MenuItem.OnMenuItemClickListener listener = item -> handleMenuClickByTag(Objects.requireNonNull(menuTags.get(item)));

        // Assign the listener to each menu item
        for (MenuItem menuItem : menuTags.keySet()) {
            menuItem.setOnMenuItemClickListener(listener);
        }

        return true;
    }

    // Handle menu item clicks based on their associated tags
    private boolean handleMenuClickByTag(String tag) {
        Intent intent;
        switch (tag) {
            case "add_item":
                Log.d(TAG, "Navigating to Add Item view");
                intent = new Intent(this, EditItemActivity.class);
                startActivity(intent);
                return true;
            case "toggle_notifications":
                Log.d(TAG, "Navigating to SMS Notifications view");
                intent = new Intent(this, SmsNotificationsActivity.class);
                startActivity(intent);
                return true;
            case "logout":
                Log.d(TAG, "Logging out and navigating to Login view");
                intent = new Intent(this, LoginActivity.class);
                startActivity(intent);
                return true;
            default:
                return false;
        }
    }

    // Update the UI to show or hide the empty state based on the item list
    public void checkListIsEmpty() {
        Log.d(TAG, "Current inventory size: " + (mItemList != null ? mItemList.size() : 0));
        if (mItemList == null || mItemList.isEmpty()) {
            itemListView.setVisibility(View.GONE); // Hide the item list view
            emptyListView.setVisibility(View.VISIBLE); // Show the empty state message
        } else {
            itemListView.setVisibility(View.VISIBLE); // Show the item list view
            emptyListView.setVisibility(View.GONE); // Hide the empty state message
        }
    }
}
