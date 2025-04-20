const mongoose = require('mongoose');
const bcrypt = require('bcrypt');
const jwt = require('jsonwebtoken');
const { Schema } = mongoose;

// User Schema definition
const userSchema = new Schema(
    {
        // Email field: ensures uniqueness, required, and matches the email format.
        email: {
            type: String,
            unique: true,  // Ensures no duplicate emails in the database
            required: [true, 'Email is required'],  // Makes email a required field
            lowercase: true,  // Ensures email is stored in lowercase
            match: [/^\S+@\S+\.\S+$/, 'Please provide a valid email address'],  // Regex for email validation
        },

        // Name field: required, trimmed to remove any leading/trailing spaces
        name: {
            type: String,
            required: [true, 'Name is required'],  // Makes name a required field
            trim: true,  // Removes extra spaces before/after the name
        },

        // Role field: defines the user's role and ensures only allowed values are entered
        role: {
            type: String,
            required: [true, 'Role is required'],  // Ensures role is provided
            enum: ['Admin', 'SuperUser', 'User'],  // Restricts the role to these specific values
            default: 'User',  // Default to 'User' if no role is provided
        },

        // Password hash field: stores the hashed password, required for user authentication
        passwordHash: {
            type: String,
            required: true,  // Ensures password hash is always present
        },
    },
    { timestamps: true }  // Automatically adds 'createdAt' and 'updatedAt' fields
);

// Method to set (hash) the password
userSchema.methods.setPassword = async function (password) {
    try {
        const saltRounds = 10;  // Defines the number of salt rounds for bcrypt
        this.passwordHash = await bcrypt.hash(password, saltRounds);  // Hashes the password and saves it
    } catch (error) {
        throw new Error('Error hashing the password');  // Error handling for bcrypt hashing failure
    }
};

// Method to validate a given password against the hashed password
userSchema.methods.validPassword = async function (password) {
    try {
        return await bcrypt.compare(password, this.passwordHash);  // Compares the given password with the stored hash
    } catch (error) {
        throw new Error('Error validating password');  // Error handling for bcrypt comparison failure
    }
};

// Method to generate a JWT token for the user
userSchema.methods.generateJwt = function () {
    try {
        // Payload for the JWT (usually contains the user's unique identifier and role)
        const payload = {
            email: this.email,
            role: this.role,
        };

        // Generate JWT with the payload and expiration time of 1 hour
        return jwt.sign(payload, process.env.JWT_SECRET, { expiresIn: '1h' });
    } catch (error) {
        throw new Error('Error generating JWT');  // Error handling for JWT generation failure
    }
};

// Static method to find a user by email (used for login or authentication)
userSchema.statics.findByEmail = async function (email) {
    try {
        return await this.findOne({ email: email.toLowerCase() });  // Case-insensitive search for the email
    } catch (error) {
        throw new Error('Error finding user by email');  // Error handling for user lookup failure
    }
};

// Index on email for improved query performance
userSchema.index({ email: 1 });  // Indexes the email field to make lookups faster

// Pre-save middleware to check if JWT_SECRET is set before saving a user
userSchema.pre('save', function (next) {
    // If JWT_SECRET is not set, throw an error
    if (!process.env.JWT_SECRET) {
        throw new Error('JWT_SECRET environment variable is not set');
    }
    next();  // Proceed to save the user
});

// Export the User model with the schema
module.exports = mongoose.model('User', userSchema);
