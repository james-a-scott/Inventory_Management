const mongoose = require('mongoose');
const Item = require('../models/items'); // Register Model for item schema
const Model = mongoose.model('items'); // Access the 'items' model

// GET: /items - lists all the items
// This endpoint retrieves all items from the database
const itemsList = async (req, res) => {
    try {
        // Retrieve all items from the database
        const items = await Model.find({}).exec();

        // If no items are found, respond with 404 status
        if (!items || items.length === 0) {
            return res.status(404).json({ message: 'No items found' });
        } else {
            // Successfully retrieved items, return them with 200 status
            return res.status(200).json(items);
        }
    } catch (err) {
        // Catch any errors during the database operation and return 500 status with error message
        return res.status(500).json({ message: 'Server error', error: err.message });
    }
};

// GET: /items/:itemCode - lists a single item by code
// This endpoint retrieves a specific item based on its unique code
const itemsFindByCode = async (req, res) => {
    try {
        // Retrieve the item using the 'code' parameter from the request URL
        const item = await Model.findOne({ code: req.params.itemCode }).exec();

        // If item is not found, respond with 404 status
        if (!item) {
            return res.status(404).json({ message: 'Item not found' });
        } else {
            // Successfully retrieved the item, return it with 200 status
            return res.status(200).json(item);
        }
    } catch (err) {
        // Catch any errors during the database operation and return 500 status with error message
        return res.status(500).json({ message: 'Server error', error: err.message });
    }
};

// POST: /items - Adds a new item to the inventory
// This endpoint allows the user to add a new item to the inventory database
const itemsAddItem = async (req, res) => {
    try {
        // Extract fields from request body
        const { code, name, quantity } = req.body;

        // Validate that all required fields are provided
        if (!code || !name || !quantity) {
            return res.status(400).json({ message: 'Missing required fields (code, name, quantity)' });
        }

        // Create a new item instance with the provided data
        const newItem = new Item({
            code,
            name,
            quantity,
        });

        // Save the new item to the database
        const savedItem = await newItem.save();

        // Return the saved item with 201 status (created)
        return res.status(201).json(savedItem);
    } catch (err) {
        // Catch any errors during the item creation process and return 400 status with error message
        return res.status(400).json({ message: 'Error creating item', error: err.message });
    }
};

// PUT: /items/:itemCode - Updates an existing item based on itemCode
// This endpoint updates an existing item in the database by its unique code
const itemsUpdateItem = async (req, res) => {
    try {
        // Extract fields from request body
        const { code, name, quantity } = req.body;

        // Validate that all required fields are provided
        if (!code || !name || !quantity) {
            return res.status(400).json({ message: 'Missing required fields (code, name, quantity)' });
        }

        // Find the item by its code and update it with the new values
        const updatedItem = await Model.findOneAndUpdate(
            { code: req.params.itemCode }, // Search for the item by code
            { code, name, quantity },      // Fields to update
            { new: true }                  // Return the updated item (default is the original)
        ).exec();

        // If item is not found, respond with 404 status
        if (!updatedItem) {
            return res.status(404).json({ message: 'Item not found' });
        } else {
            // Successfully updated the item, return the updated item with 200 status
            return res.status(200).json(updatedItem);
        }
    } catch (err) {
        // Catch any errors during the update process and return 400 status with error message
        return res.status(400).json({ message: 'Error updating item', error: err.message });
    }
};

// DELETE: /items/:itemCode - Deletes an item by itemCode
// This endpoint deletes an existing item from the inventory database by its unique code
const itemsDeleteItem = async (req, res) => {
    try {
        // Find the item by its code and delete it
        const deletedItem = await Model.findOneAndDelete({ code: req.params.itemCode }).exec();

        // If item is not found, respond with 404 status
        if (!deletedItem) {
            return res.status(404).json({ message: 'Item not found' });
        } else {
            // Successfully deleted the item, return a success message with 200 status
            return res.status(200).json({ message: 'Item deleted successfully' });
        }
    } catch (err) {
        // Catch any errors during the delete process and return 500 status with error message
        return res.status(500).json({ message: 'Error deleting item', error: err.message });
    }
};

// Export all the CRUD functions to be used in the routes
module.exports = {
    itemsList,         // Export the list function
    itemsFindByCode,   // Export the find function by item code
    itemsAddItem,      // Export the add item function
    itemsUpdateItem,   // Export the update item function
    itemsDeleteItem    // Export the delete item function
};
