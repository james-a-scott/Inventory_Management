const passport = require("passport");
const LocalStrategy = require("passport-local").Strategy;
const mongoose = require("mongoose");
const User = require("../models/user");

// Configure Passport's local strategy
passport.use(
    new LocalStrategy(
        {
            usernameField: "email",  // Use 'email' as the username field for authentication
        },
        async (username, password, done) => {
            try {
                // Attempt to find the user by email (username). The 'exec()' method is used to execute the query and return a promise.
                const user = await User.findOne({ email: username }).exec();

                // If no user is found, return a message indicating incorrect username
                if (!user) {
                    return done(null, false, { message: 'Incorrect username.' });
                }

                // Validate the provided password by calling the instance method 'validPassword'
                // This method should compare the hashed password stored in the database with the provided password
                const isValidPassword = await user.validPassword(password);
                if (!isValidPassword) {
                    // If the password is invalid, return a message indicating incorrect password
                    return done(null, false, { message: 'Incorrect password.' });
                }

                // If user is found and password is valid, return the user object to indicate successful authentication
                return done(null, user);
            } catch (err) {
                // Catch any errors during the authentication process (e.g., database errors, network issues)
                console.error("Error during user authentication:", err);

                // Pass the error to the done function to ensure the authentication process can fail gracefully
                return done(err);
            }
        }
    )
);
