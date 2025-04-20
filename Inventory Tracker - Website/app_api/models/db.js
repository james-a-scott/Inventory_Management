// Import necessary dependencies
const mongoose = require('mongoose');
const host = process.env.DB_HOST || '127.0.0.1';  // Get database host from environment variable or default to localhost
const dbURI = `mongodb://${host}/inventory`;  // Build the MongoDB connection URI using the host
const readLine = require('readline');  // Import readline module for handling specific platform signals

// Function to establish the MongoDB connection after a delay
// The timeout is set to ensure the connection is attempted after a short delay.
const connect = async () => {
    try {
        // Attempt to connect to MongoDB after 1 second
        setTimeout(async () => {
            await mongoose.connect(dbURI, {});
            console.log(`Mongoose connected to ${dbURI}`);
        }, 1000);
    } catch (err) {
        console.error('Error connecting to MongoDB:', err);
    }
}

// Monitor Mongoose connection events
mongoose.connection.on('connected', () => {
    // Log when Mongoose successfully connects to the database
    console.log(`Mongoose connected to ${dbURI}`);
});

// Log any connection errors
mongoose.connection.on('error', err => {
    console.log('Mongoose connection error: ', err);  // Error message if connection fails
});

// Log when Mongoose disconnects from the database
mongoose.connection.on('disconnected', () => {
    console.log('Mongoose disconnected');  // Message on successful disconnection
});

// Handle SIGINT signal specifically for Windows platform
if (process.platform === 'win32') {
    // Use readline to handle Ctrl+C (SIGINT) signal on Windows
    const r1 = readLine.createInterface({
        input: process.stdin,
        output: process.stdout
    });
    r1.on('SIGINT', () => {
        // Emit SIGINT event when Ctrl+C is pressed
        process.emit("SIGINT");
    });
}

// Function to handle graceful shutdown of the application
// This ensures that MongoDB connection is closed properly when the app is terminated
const gracefulShutdown = async (msg) => {
    try {
        // Close the mongoose connection
        await mongoose.connection.close();
        console.log(`Mongoose disconnected through ${msg}`);
    } catch (err) {
        console.error('Error during graceful shutdown:', err);
    }
};

// Event listener to handle graceful shutdown on nodemon restart (useful for development)
process.once('SIGUSR2', () => {
    // Gracefully shut down on nodemon restart
    gracefulShutdown('nodemon restart');
    process.kill(process.pid, 'SIGUSR2');  // Send SIGUSR2 signal to terminate the process
});

// Event listener for app termination (Ctrl+C or process exit)
process.on('SIGINT', () => {
    // Gracefully shut down on app termination
    gracefulShutdown('app termination');
    process.exit(0);  // Exit the process after the shutdown
});

// Event listener for container termination (e.g., Docker container stop)
process.on('SIGTERM', () => {
    // Gracefully shut down on container termination
    gracefulShutdown('app shutdown');
    process.exit(0);  // Exit the process after the shutdown
});

// Make the initial connection to the database
connect();

// Import the Mongoose schema (in this case, the items schema)
require('./items');  // This ensures that the items model is registered with mongoose

// Export mongoose so that it can be used in other parts of the application
module.exports = mongoose;
