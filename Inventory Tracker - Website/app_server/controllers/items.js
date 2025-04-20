const itemsEndpoint = "http://localhost:3000/api/items";
const options = {
  method: "GET",
  headers: {
    Accept: "application/json",
  },
};

const items = async function (req, res, next) {
  // console.log('TRAVEL CONTROLLER BEGIN");
  await fetch(itemsEndpoint, options)
    .then((res) => res.json())
    .then((json) => {
      let message = null;
      if (!(json instanceof Array)) {
        message = "API lookup error";
        json = [];
      } else {
        if (!json.length) {
          message = "No items exist in our database!";
        }
      }
      res.render("items", { title: "Items - Innventory Site", items: json });
    })
    .catch((err) => res.status(500).send(err.message));
};

module.exports = {
  items,
};