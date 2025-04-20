const mongoose = require('mongoose');

// Define the item schema for the inventory
const itemSchema = new mongoose.Schema({
    // Code field: required and indexed to ensure quick lookups by item code
    code: {
        type: String,  // Specifies the type of data (string)
        required: true,  // Ensures this field is always provided
        index: true,  // Creates an index on this field for faster queries
    },

    // Name field: required and indexed to allow quick lookups by item name
    name: {
        type: String,  // Specifies the type of data (string)
        required: true,  // Ensures this field is always provided
        index: true,  // Creates an index on this field for faster queries
    },

    // Quantity field: required to track the number of items
    quantity: {
        type: Number,  // Specifies the type of data (number)
        required: true,  // Ensures this field is always provided
    },
},
    {
        timestamps: true,  // Automatically adds 'createdAt' and 'updatedAt' fields for tracking
    });

// Create the 'Inventory' model based on the item schema
const Inventory = mongoose.model('Item', itemSchema);

// Export the Inventory model for use in other parts of the application
module.exports = Inventory;
