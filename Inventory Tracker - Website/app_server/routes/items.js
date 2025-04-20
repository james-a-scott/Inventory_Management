const express = require('express');
const router = express.Router();
const controller = require('../controllers/items');

/* GET items page. */
router.get('/', controller.items);

module.exports = router;
