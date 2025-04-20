/* GET About View */
const about = (req, res) => {
  res.render('about', {title: "About - Inventory Site"});
  };
  
  module.exports = {
    about,
  };