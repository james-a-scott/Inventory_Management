package com.example.inventoryapplication;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NavUtils;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

/**
 * This activity functions as the "create new" and "edit" inventory items screen.
 */
public class EditItemActivity extends AppCompatActivity {

    // The name of the key to use when sending an item to another view
    public static final String EXTRA_ITEM = "com.example.inventoryapplication.item";

    // Instance of the inventory database
    InventoryDatabase inventoryDatabase;

    // EditText fields for item name and quantity
    EditText itemName;
    EditText itemQuantity;

    // Buttons for saving and deleting items
    Button saveBtn;
    Button deleteItemBtn;

    // The current item being edited. `null` if this is a new item not saved yet.
    private Item mItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Set up this view's transitions
        overrideActivityTransition(R.anim.slide_in_right, R.anim.slide_out_left, 0);

        // Set the content view for this activity
        setContentView(R.layout.activity_edit_item);

        // Initialize the inventory database instance
        inventoryDatabase = InventoryDatabase.getInstance(this);

        // Cache the views for item name, quantity, and action buttons
        itemName = findViewById(R.id.editItemName);
        itemQuantity = findViewById(R.id.editQuantity_edit);
        deleteItemBtn = findViewById(R.id.deleteItemBtn);
        saveBtn = findViewById(R.id.saveItem);

        // Set the initial state of the action buttons
        deleteItemBtn.setVisibility(View.GONE);
        saveBtn.setEnabled(false); // Disable save button until input is provided

        int initialQuantity = 0; // Initialize quantity variable

        // Check if an item was passed as serialized data; if so, populate the fields
        Item item = getIntent().getSerializableExtra(EXTRA_ITEM, Item.class);
        if (item != null) {
            mItem = item; // Assign the item to the class variable
            itemName.setText(item.getName()); // Set item name in the EditText
            initialQuantity = item.getQuantity(); // Get initial quantity from the item
            deleteItemBtn.setVisibility(View.VISIBLE); // Show delete button for existing items
        }

        // Set the initial quantity value in the EditText
        itemQuantity.setText(String.valueOf(initialQuantity));

        // Listen for text changes in the item name and quantity fields
        itemName.addTextChangedListener(textWatcher);
        itemQuantity.addTextChangedListener(textWatcher);
    }

    /**
     * Listen for text changes on the item name and quantity fields and set the save button
     * to enabled or disabled based on whether there is text in the item name field or not.
     */
    private final TextWatcher textWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            // No action needed before text is changed
        }

        @Override
        public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
            // Enable save button if item name is not empty
            saveBtn.setEnabled(!getItemName().isEmpty());
        }

        @Override
        public void afterTextChanged(Editable s) {
            // No action needed after text is changed
        }
    };

    /**
     * Save an item to the database (insert or update).
     *
     * @param view Instance of the current view
     */
    public void handleSaveItem(View view) {
        boolean saved;

        // If editing an existing item, update its values in the database
        if (mItem != null) {
            mItem.setName(getItemName());
            mItem.setQuantity(getItemQuantity());
            saved = inventoryDatabase.updateItem(mItem); // Update item in the database
        } else {
            // Create a new item in the database
            saved = inventoryDatabase.addItem(getItemName(), getItemQuantity());
        }

        // If the item saved successfully, navigate back; otherwise, show an error message
        if (saved) {
            NavUtils.navigateUpFromSameTask(this);
        } else {
            Toast.makeText(EditItemActivity.this, R.string.save_error, Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Handle deleting the current item if we're editing an existing one.
     *
     * @param view Instance of the current view
     */
    public void handleDeleteItem(View view) {
        // Wrap the delete action in a confirmation dialog
        new AlertDialog.Builder(this)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle(R.string.delete_confirmation_title)
                .setMessage(R.string.delete_confirmation)
                .setPositiveButton("Yes", (dialog, which) -> {
                    // Delete the item from the database
                    boolean deleted = inventoryDatabase.deleteItem(mItem);
                    finish(); // Close the activity

                    // Navigate back if deleted successfully; otherwise, show an error message
                    if (deleted) {
                        NavUtils.navigateUpFromSameTask(EditItemActivity.this);
                    } else {
                        Toast.makeText(EditItemActivity.this, R.string.delete_error, Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("No", null)
                .show(); // Display the dialog
    }

    /**
     * Increase the item's quantity by one.
     *
     * @param view Instance of the current view
     */
    public void incrementQuantity(View view) {
        itemQuantity.setText(String.valueOf(getItemQuantity() + 1)); // Increment quantity
    }

    /**
     * Decrease the item's quantity by one, stopping at zero.
     *
     * @param view Instance of the current view
     */
    public void decrementQuantity(View view) {
        itemQuantity.setText(String.valueOf(Math.max(0, getItemQuantity() - 1))); // Decrement quantity
    }

    /**
     * Helper method to get the current item's name from the text field.
     *
     * @return The item's name
     */
    private String getItemName() {
        Editable name = itemName.getText(); // Get item name from EditText
        return name != null ? name.toString().trim() : ""; // Return trimmed name
    }

    /**
     * Helper method to get the current item's quantity by parsing the integer from the text field.
     *
     * @return The item's quantity
     */
    private int getItemQuantity() {
        String rawValue = itemQuantity.getText().toString().replaceAll("[^\\d.]", "").trim(); // Clean input
        int quantity = rawValue.isEmpty() ? 0 : Integer.parseInt(rawValue); // Parse quantity

        // Ensure quantity cannot be less than 0
        return Math.max(quantity, 0);
    }
}
