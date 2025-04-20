const passport = require("passport");
const mongoose = require("mongoose");
const User = require("../models/user");

// Register function for user sign-up
const register = async (req, res) => {
    // Ensure all required fields (name, email, role, password) are present
    if (!req.body.name || !req.body.email || !req.body.role || !req.body.password) {
        return res.status(400).json({ message: "All fields required" });  // Respond with 400 Bad Request if any field is missing
    }

    // Create a new user instance
    const user = new User();
    user.name = req.body.name;  // Assign user name
    user.email = req.body.email;  // Assign user email
    user.role = req.body.role;  // Assign user role

    try {
        // Hash the user's password using bcrypt before saving it
        await user.setPassword(req.body.password);

        // Attempt to save the new user to the database
        const savedUser = await user.save();

        // Log success for debugging and monitoring purposes
        console.log('User saved:', savedUser);

        // Respond with 200 OK status and success message
        res.status(200).json({ message: 'User registered successfully' });
    } catch (err) {
        // Catch any errors during registration (e.g., saving the user, password hashing)
        console.error('Registration error:', err);

        // Respond with 400 Bad Request status and error details
        res.status(400).json({ message: 'Error during registration', error: err.message });
    }
};

// Login function for user authentication
const login = (req, res) => {
    // Ensure email and password fields are present in the request body
    if (!req.body.email || !req.body.password) {
        return res.status(400).json({ message: "All fields required" });  // Respond with 400 Bad Request if any field is missing
    }

    // Passport authentication using the local strategy
    passport.authenticate("local", (err, user, info) => {
        try {
            // If an error occurred during authentication (e.g., database connection error)
            if (err) {
                console.error('Authentication error:', err);  // Log the error for debugging
                return res.status(404).json({ message: 'Authentication failed', error: err.message });  // Respond with 404 Not Found
            }

            // If user is authenticated successfully
            if (user) {
                const token = user.generateJwt();  // Generate a JWT for the authenticated user
                return res.status(200).json({ token });  // Respond with 200 OK status and the JWT token
            } else {
                // If authentication fails (invalid credentials), return a 401 Unauthorized status with the info object from Passport
                return res.status(401).json(info);
            }
        } catch (authErr) {
            // Catch any unexpected errors during the authentication process
            console.error('Unexpected authentication error:', authErr);  // Log the error for debugging
            return res.status(500).json({ message: 'Server error during authentication', error: authErr.message });  // Respond with 500 Internal Server Error
        }
    })(req, res);
};

// Export the register and login functions for use in routing
module.exports = {
    register,
    login,
};
