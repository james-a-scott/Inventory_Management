// Bring in the DB connection and the Trip schema
const Mongoose = require("./db");
const Inventory = require("./inventory");

//Read seed data from json file
var fs = require("fs");
var items = JSON.parse(fs.readFileSync("./data/items.json", "utf8"));

// delte any existing records, then insert seed data
const seedDB = async () => {
  await Inventory.deleteMany({});
  await Inventory.insertMany(items);
};

//Close the MongoDB connection and exit
seedDB().then(async () => {
  await Mongoose.connection.close();
  process.exit(0);
});