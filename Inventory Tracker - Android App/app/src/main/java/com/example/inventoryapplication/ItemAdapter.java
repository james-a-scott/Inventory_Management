package com.example.inventoryapplication;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import android.telephony.SmsManager;
import android.content.SharedPreferences;

/**
 * Adapter class for handling inventory item views in a RecyclerView.
 * This adapter binds the list of inventory items to the corresponding views, handles
 * item-specific actions like editing and deleting, and manages visibility of the
 * edit and delete buttons based on the user's role (Admin/SuperUser).
 * This class also handles network operations for item deletion using Volley.
 */
public class ItemAdapter extends RecyclerView.Adapter<ItemAdapter.ItemViewHolder> {

    private final List<Item> itemList;  // List of items to be displayed in the RecyclerView
    private final Context context;       // The context used for UI operations and network requests
    private final String authToken;      // Authorization token used for making authenticated requests
    private final String userRole;      // Role of the user (Admin/SuperUser) to determine button visibility
    private final ActivityResultLauncher<Intent> editItemLauncher;  // Launcher to handle editing activity result

    /**
     * Constructor to initialize the adapter with necessary data.
     * @param itemList        List of items to be displayed in the RecyclerView.
     * @param context         The context for launching activities and making network requests.
     * @param authToken       The authentication token for authorized requests.
     * @param userRole        The role of the current user (determines button visibility).
     * @param launcher        The ActivityResultLauncher for launching the item editing activity.
     */
    public ItemAdapter(List<Item> itemList, Context context, String authToken, String userRole, ActivityResultLauncher<Intent> launcher) {
        this.itemList = itemList;
        this.context = context;
        this.authToken = authToken;
        this.userRole = userRole;
        this.editItemLauncher = launcher;
    }

    /**
     * Creates a new ViewHolder instance to hold item views for the RecyclerView.
     * @param parent   The parent view group that holds the item views.
     * @param viewType The view type for the item (not used in this case).
     * @return A new ViewHolder object to hold the item view.
     */
    @NonNull
    @Override
    public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        try {
            // Inflate the layout for each inventory item in the RecyclerView.
            View itemView = LayoutInflater.from(context).inflate(R.layout.inventory_item, parent, false);
            return new ItemViewHolder(itemView);
        } catch (Exception e) {
            // Log error if the layout cannot be inflated
            Log.e("ItemAdapter", "Error inflating view", e);
            Toast.makeText(context, "Error loading item view", Toast.LENGTH_SHORT).show();
            // Return a valid fallback ItemViewHolder to satisfy the method's return type
            View itemView = new View(context);
            return new ItemViewHolder(itemView);
        }
    }

    /**
     * Binds the data for an item at a given position in the RecyclerView.
     * @param holder   The ViewHolder that will display the item data.
     * @param position The position of the item in the itemList.
     */
    @Override
    public void onBindViewHolder(@NonNull ItemViewHolder holder, int position) {
        try {
            // Get the item data for the given position
            Item item = itemList.get(position);
            holder.itemName.setText(item.getName());
            holder.editQuantity.setText(String.valueOf(item.getQuantity()));

            // Set visibility of the edit button based on user role (Admin/SuperUser)
            if ("Admin".equalsIgnoreCase(userRole) || "SuperUser".equalsIgnoreCase(userRole)) {
                holder.editButton.setVisibility(View.VISIBLE);  // Show the edit button for Admin/SuperUser
            } else {
                holder.editButton.setVisibility(View.GONE);  // Hide the edit button for other roles
            }

            // Set visibility of the delete button for Admin only
            if ("Admin".equalsIgnoreCase(userRole)) {
                holder.deleteButton.setVisibility(View.VISIBLE);  // Show the delete button for Admin
            } else {
                holder.deleteButton.setVisibility(View.GONE);  // Hide the delete button for other roles
            }

            // Set up click listener for the edit button
            holder.editButton.setOnClickListener(v -> {
                try {
                    // Check if the editItemLauncher is available, else show an error
                    Log.d("ItemAdapter", "Edit button clicked for item: " + item.getName());
                    if (editItemLauncher == null) {
                        Log.e("ItemAdapter", "Error: editItemLauncher is null. Cannot launch edit activity.");
                        Toast.makeText(context, "Unable to open editor. Please try again later.", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // Launch the EditItemActivity to edit the current item
                    Intent intent = new Intent(context, EditItemActivity.class);
                    intent.putExtra("ITEM_NAME", item.getName());
                    intent.putExtra("ITEM_QUANTITY", item.getQuantity());
                    intent.putExtra("ITEM_CODE", item.getCode());
                    editItemLauncher.launch(intent);
                } catch (Exception e) {
                    // Catch any errors while launching the edit activity
                    Log.e("ItemAdapter", "Error launching edit activity", e);
                    Toast.makeText(context, "Error launching editor: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });

            // Set up click listener for the delete button
            holder.deleteButton.setOnClickListener(v -> showDeleteConfirmationDialog(position, item));

        } catch (Exception e) {
            // Catch any errors while binding the view
            Log.e("ItemAdapter", "Error binding item view", e);
            Toast.makeText(context, "Error displaying item", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Returns the total number of items in the list.
     * @return The total number of items in the itemList.
     */
    @Override
    public int getItemCount() {
        return itemList.size();
    }

    /**
     * Deletes an item from the server by sending a DELETE request to the API.
     * @param item     The item to be deleted.
     * @param position The position of the item in the itemList.
     */
    private void deleteItemFromServer(Item item, int position) {
        try {
            // URL for the API to delete the item using its unique code
            String url = "http://192.168.86.33:3000/api/items/" + item.getCode();

            // Make a DELETE request using Volley to delete the item
            StringRequest stringRequest = new StringRequest(Request.Method.DELETE, url,
                    response -> {
                        // If the deletion is successful, remove the item from the list and update the UI
                        itemList.remove(position);
                        notifyItemRemoved(position);
                        Toast.makeText(context, "Item deleted", Toast.LENGTH_SHORT).show();
                    },
                    error -> {
                        // Log and show an error message if the deletion fails
                        Log.e("ItemAdapter", "Error deleting item: " + error.getMessage(), error);
                        Toast.makeText(context, "Failed to delete item", Toast.LENGTH_SHORT).show();
                    }
            ) {
                @Override
                public Map<String, String> getHeaders() {
                    // Add authorization header to the request
                    Map<String, String> headers = new HashMap<>();
                    headers.put("Authorization", "Bearer " + authToken);
                    return headers;
                }
            };

            // Add the request to the Volley request queue
            Volley.newRequestQueue(context).add(stringRequest);
        } catch (Exception e) {
            // Catch any errors during the delete operation
            Log.e("ItemAdapter", "Error in deleteItemFromServer", e);
            Toast.makeText(context, "Error deleting item from server", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Displays a confirmation dialog asking the user if they are sure about deleting the item.
     * @param position The position of the item in the itemList.
     * @param item     The item to be deleted.
     */
    private void showDeleteConfirmationDialog(int position, Item item) {
        try {
            new AlertDialog.Builder(context)
                    .setMessage("Are you sure you want to delete this item?")
                    .setPositiveButton("Yes", (dialog, which) -> deleteItemFromServer(item, position))  // Confirm delete
                    .setNegativeButton("No", null)  // Cancel delete
                    .show();
        } catch (Exception e) {
            // Catch any errors displaying the delete confirmation dialog
            Log.e("ItemAdapter", "Error showing delete confirmation dialog", e);
            Toast.makeText(context, "Error displaying confirmation dialog", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * ViewHolder class that holds the views for displaying a single inventory item.
     * This class binds the item views (name, quantity, and buttons) and handles user interactions.
     */
    public static class ItemViewHolder extends RecyclerView.ViewHolder {
        TextView itemName, editQuantity;  // Views to display the item name and quantity
        ImageButton editButton, deleteButton;  // Buttons for editing and deleting the item

        /**
         * Constructor for initializing the ViewHolder with the item views.
         * @param itemView The view containing the item views to be bound.
         */
        public ItemViewHolder(View itemView) {
            super(itemView);
            itemName = itemView.findViewById(R.id.itemName);
            editQuantity = itemView.findViewById(R.id.editQuantity);
            editButton = itemView.findViewById(R.id.editButton);
            deleteButton = itemView.findViewById(R.id.deleteButton);
        }
    }
}
