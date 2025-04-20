/*GET Homepage */
const index = (req, res) => {
    res.render('index', {title: "Home - Inventory Site"});
};

module.exports = {
    index
};