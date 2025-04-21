# Inventory Management

## Overview

**Inventory Management** is a cross-platform project originally developed as a mobile application in **CS360: Mobile Architecture and Programming**, and later expanded to include a full-stack web solution. The goal of this project is to provide an efficient, user-friendly inventory tracking system for environments like warehouses and retail spaces, with a focus on real-time visibility, security, and scalability.

---

## 📱 Original Artifact: Mobile App (Android)

The initial version of the project is located in:  
`Inventory_Management/Inventory Tracker - Original Artifact/`

This mobile app was designed to:
- Track inventory in real-time
- Display items in a grid format
- Allow users to add, remove, and adjust quantities of items
- Alert users when an item count reaches zero
- Use local storage with SQLite to store login credentials and inventory data
- Feature basic user authentication

---

## 🚀 Enhanced Artifacts

Over time, the project evolved significantly to demonstrate growth in three major areas:

### 🔧 Software Design and Engineering
- Migrated core functionality of the mobile app to a **MEAN stack (MongoDB, Express.js, Angular, Node.js)** website  
- Created a modern, responsive UI for the web version  
- Enabled seamless interaction across devices

### 🧠 Algorithms and Data Structures
- Refactored and optimized the Android app to prepare for integration with the website backend  
- Introduced advanced data handling, client-server communication, and synchronization logic

### 🗄️ Databases
- Integrated a **shared MongoDB database** for both the web and mobile apps  
- Implemented **Role-Based Access Control (RBAC)**  
- Enhanced password security by migrating from basic hash storage to **bcrypt** encryption  
- Added **search functionality and pagination** to improve data access and performance  
- Enabled the mobile app to interact with the web APIs for unified data access

### 📁 Enhanced Artifact Structure
- Located in: `Inventory_Management/Inventory Tracker - Android App/`
- Located in: `Inventory_Management/Inventory Tracker - Website/`
- Located in: `Inventory_Management/Inventory Tracker - MongoDB/`

---

## 🎥 Code Review and Narratives

To provide deeper insight into the enhancements and decision-making process, the project includes:

- **Code Review (Video Walkthrough)**: [YouTube.com/site/](https://youtube.com/site/)  
- **Narratives and Explanations**:  
  Located in: `Inventory_Management/Narratives/`

---

## 🎯 Project Goals and Learning Outcomes

This project addresses the following Computer Science program objectives:

- **Collaboration**: Employ strategies for collaborative environments that support organizational decision-making  
- **Communication**: Deliver professional-quality oral, written, and visual documentation  
- **Problem Solving**: Design and evaluate solutions using algorithmic principles and appropriate CS standards  
- **Implementation**: Use innovative tools and techniques to build real-world software solutions  
- **Security**: Integrate security principles to anticipate and mitigate vulnerabilities

---

## 🛠️ Tech Stack

**Mobile App**
- Java (Android)
- SQLite

**Website**
- Angular
- Node.js
- Express.js
- MongoDB
- RESTful APIs

**Security**
- Bcrypt (password hashing)
- Role-Based Access Control

---

## 📦 Installation & Setup

### For Mobile App
1. Open the Android project in Android Studio.
2. Run the emulator or connect an Android device.
3. Build and run the app.

### For Web App
1. Navigate to `Inventory Tracker - Website/`
2. Run `npm install` to install dependencies
3. Run `ng serve` (Angular) for frontend
4. Run the Node.js/Express backend server (`node server.js`)

### MongoDB Setup
- Ensure MongoDB is running locally or via cloud (e.g., MongoDB Atlas)
- Use the provided scripts/configs under `Inventory Tracker - MongoDB/` to seed data

---

## 📬 Contributions

This is an academic project, but contributions are welcome for educational purposes. Feel free to fork, explore, and provide suggestions or improvements.

---

## 📄 License

This project is licensed for educational use. Please contact the repository owner for more information on reuse or distribution.

---

## 🙌 Acknowledgements

Developed as part of the **Southern New Hampshire University** Computer Science program. 
