package com.example.inventoryapplication;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Parcel;
import android.os.Parcelable;
import android.telephony.SmsManager;
import android.util.Log;
import androidx.annotation.NonNull;

/**
 * Represents a single inventory item and its associated metadata.
 * This class is used to encapsulate and transfer item data between components,
 * support data persistence, and handle notification logic when inventory is depleted.
 * Implements {@link Parcelable} to allow instances to be passed between activities.
 */
public class Item implements Parcelable {

    // Used for logging purposes throughout this class
    private static final String TAG = "Item";

    /** Unique MongoDB Object ID as a string. Can be null for unsaved items. */
    private String mId;

    /** Internal item code. Used for search, filtering, or SKU reference. */
    private String mCode;

    /** Display name for the item. This is what users typically see in the UI. */
    private String mName;

    /** Total quantity currently in stock. Must not be negative. */
    private int mQuantity;

    /**
     * Full constructor for instantiating an item.
     * @param id       MongoDB document ID (nullable for new items not yet saved)
     * @param code     Unique identifier or stock-keeping unit (SKU) code
     * @param name     User-facing item name
     * @param quantity Quantity of the item; must be >= 0
     */
    public Item(String id, String code, String name, int quantity) {
        try {
            this.mId = id;
            this.mCode = code;
            this.mName = name;
            this.mQuantity = quantity;
        } catch (Exception e) {
            Log.e(TAG, "Exception in Item constructor: " + e.getMessage(), e);
        }
    }

    /**
     * Sets the MongoDB document ID. Should only be used after the object is saved.
     * @param id MongoDB-generated unique ID
     */
    public void setId(String id) {
        try {
            this.mId = id;
        } catch (Exception e) {
            Log.e(TAG, "setId failed: " + e.getMessage(), e);
        }
    }

    /**
     * Gets the MongoDB ID used to uniquely identify the item in the database.
     * @return MongoDB _id field as String, or null if not yet assigned
     */
    public String getId() {
        return mId;
    }

    /**
     * Sets the internal code used to categorize or look up the item.
     * @param code Alphanumeric string representing the item code
     */
    public void setCode(String code) {
        try {
            this.mCode = code;
        } catch (Exception e) {
            Log.e(TAG, "setCode failed: " + e.getMessage(), e);
        }
    }

    /**
     * Gets the internal code used to identify the item.
     * @return Item code string
     */
    public String getCode() {
        return mCode;
    }

    /**
     * Gets the name of the item. Used in the UI.
     * @return Display name of the item
     */
    public String getName() {
        return mName;
    }

    /**
     * Sets the user-friendly name of the item. Should not be empty.
     * @param name Display name shown in UI lists or alerts
     */
    public void setName(String name) {
        try {
            this.mName = name;
        } catch (Exception e) {
            Log.e(TAG, "setName failed: " + e.getMessage(), e);
        }
    }

    /**
     * Gets the current stock quantity of the item.
     * @return Quantity in inventory
     */
    public int getQuantity() {
        return mQuantity;
    }

    /**
     * Sets the current quantity. Quantity should be zero or positive.
     * @param quantity Inventory count; negative values should be avoided
     */
    public void setQuantity(int quantity) {
        try {
            this.mQuantity = quantity;
        } catch (Exception e) {
            Log.e(TAG, "setQuantity failed: " + e.getMessage(), e);
        }
    }

    /**
     * Constructs a new Item object from a Parcel.
     * Called when restoring from a saved state or inter-component transfer.
     * @param in Parcel containing serialized item data
     */
    protected Item(Parcel in) {
        try {
            mId = in.readString();
            mName = in.readString();
            mQuantity = in.readInt();
        } catch (Exception e) {
            Log.e(TAG, "Parcel read error: " + e.getMessage(), e);
        }
    }

    /**
     * Serializes the item’s data into a Parcel.
     * Useful for passing Item objects between Android components.
     * @param dest  Parcel in which data should be written
     * @param flags Flags for writing parcelable data
     */
    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        try {
            dest.writeString(mId);
            dest.writeString(mName);
            dest.writeInt(mQuantity);
        } catch (Exception e) {
            Log.e(TAG, "Parcel write error: " + e.getMessage(), e);
        }
    }

    /**
     * Describes the content type. Usually returns 0.
     * @return Bitmask indicating the set of special object types marshaled
     */
    @Override
    public int describeContents() {
        return 0;
    }

    /**
     * Parcelable.Creator implementation to regenerate Item instances from Parcels.
     */
    public static final Creator<Item> CREATOR = new Creator<>() {
        @Override
        public Item createFromParcel(Parcel in) {
            try {
                return new Item(in);
            } catch (Exception e) {
                Log.e(TAG, "Failed to create item from Parcel: " + e.getMessage(), e);
                return null;
            }
        }

        @Override
        public Item[] newArray(int size) {
            try {
                return new Item[size];
            } catch (Exception e) {
                Log.e(TAG, "Failed to create item array: " + e.getMessage(), e);
                return new Item[0];
            }
        }
    };
}
