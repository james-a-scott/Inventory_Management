// -----------------------------------------------------------------------------
// Environment Configuration: Load environment variables from .env file
// This will allow sensitive configurations like API keys, database credentials, etc. to be kept out of version control.
// -----------------------------------------------------------------------------
require('dotenv').config();

// -----------------------------------------------------------------------------
// Import required modules and libraries
// These modules handle server functionality, error handling, routing, and template rendering.
// -----------------------------------------------------------------------------
var createError = require('http-errors');
var express = require('express');
var path = require('path');
var cookieParser = require('cookie-parser');
var logger = require('morgan');
var handlebars = require('hbs');

// -----------------------------------------------------------------------------
// Import route handlers for various endpoints
// These files contain the logic for handling requests at specific paths.
// -----------------------------------------------------------------------------
var indexRouter = require('./app_server/routes/index');
var usersRouter = require('./app_server/routes/users');
var itemsRouter = require('./app_server/routes/items');
var contactRouter = require("./app_server/routes/contact");
var aboutRouter = require("./app_server/routes/about");
var apiRouter = require('./app_api/routes/index');

// -----------------------------------------------------------------------------
// Initialize Express application
// This app will be used to handle requests and serve responses for the API and web pages.
// -----------------------------------------------------------------------------
var app = express();

// -----------------------------------------------------------------------------
// Import database connection and models
// This will set up the database connection and load the necessary models for data handling.
// -----------------------------------------------------------------------------
require('./app_api/models/db');

// -----------------------------------------------------------------------------
// Import and configure authentication middleware (Passport)
// Passport is used for user authentication and authorization.
// -----------------------------------------------------------------------------
var passport = require('passport');
require('./app_api/config/passport');

// -----------------------------------------------------------------------------
// View Engine Setup: Handlebars
// Set the views directory and register the Handlebars engine to render dynamic HTML.
// -----------------------------------------------------------------------------
app.set('views', path.join(__dirname, 'app_server', 'views'));

// Register Handlebars partials (reusable view components)
handlebars.registerPartials(__dirname + '/app_server/views/partials');
app.set('view engine', 'hbs');

// -----------------------------------------------------------------------------
// Middleware Configuration
// Configure various middlewares to handle requests such as logging, parsing, and static file serving.
// -----------------------------------------------------------------------------
app.use(logger('dev')); // Logger middleware for development environment
app.use(express.json()); // Middleware to parse JSON request bodies
app.use(express.urlencoded({ extended: false })); // Middleware for parsing URL-encoded data
app.use(cookieParser()); // Middleware for cookie parsing
app.use(express.static(path.join(__dirname, 'public'))); // Middleware for serving static files
app.use(passport.initialize()); // Initialize Passport for authentication

// -----------------------------------------------------------------------------
// CORS Configuration
// Set up Cross-Origin Resource Sharing (CORS) for API routes to allow external requests
// without restrictions. This can be limited further for production.
// -----------------------------------------------------------------------------
app.use('/api', (req, res, next) => {
    res.header('Access-Control-Allow-Origin', '*');
    res.header('Access-Control-Allow-Headers', 'Origin, X-Requested-With, Content-Type, Accept, Authorization');
    res.header('Access-Control-Allow-Methods', 'GET, POST, PUT, DELETE');
    next();
});

// -----------------------------------------------------------------------------
// Route Definitions
// Define the routes and map them to corresponding route handler files.
// These routes represent different parts of the application (e.g., users, items).
// -----------------------------------------------------------------------------
app.use('/index', indexRouter); // Route for homepage or index
app.use('/users', usersRouter); // Route for user-related operations
app.use('/items', itemsRouter); // Route for item-related operations
app.use('/contact', contactRouter); // Route for contact page
app.use('/about', aboutRouter); // Route for about page
app.use('/api', apiRouter); // API routes

// -----------------------------------------------------------------------------
// Error Handling: Catch 404 (Not Found) errors
// If a request doesn't match any defined routes, it will be passed to this handler.
// -----------------------------------------------------------------------------
app.use(function (req, res, next) {
    next(createError(404)); // Forward to error handler for 404 errors
});

// -----------------------------------------------------------------------------
// Unauthorized Error Handling
// Catch errors specifically related to unauthorized access and return a 401 status.
// -----------------------------------------------------------------------------
app.use((err, req, res, next) => {
    if (err.name === 'UnauthorizedError') {
        res.status(401).json({ "message": err.name + ": " + err.message });
    }
});

// -----------------------------------------------------------------------------
// Global Error Handler
// If there is any unhandled error, this will catch it and render an error page.
// In development mode, it will show the error stack.
// -----------------------------------------------------------------------------
app.use(function (err, req, res, next) {
    res.locals.message = err.message;
    res.locals.error = req.app.get('env') === 'development' ? err : {}; // Show error stack only in development
    res.status(err.status || 500); // Use error status or default to 500
    res.render('error'); // Render an error page
});

// -----------------------------------------------------------------------------
// Server Setup: Use HTTP to create and start the server
// The server listens on port 3000 and accepts external requests by binding to 0.0.0.0.
// -----------------------------------------------------------------------------
var http = require('http');
var server = http.createServer(app); // Create server using Express app

// Listen on port 3000 (adjust the IP address to accept external connections)
server.listen(3000, '0.0.0.0', () => {
    console.log("Server is running on http://0.0.0.0:3000");
});

module.exports = app; // Export app for testing or external use
