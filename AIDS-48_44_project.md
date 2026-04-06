# AIDS-48_44_project

## 1) Project Title
**Product CRUD Web + CLI Application (Core Java + JDBC + MySQL)**

## 2) Group Members
- Dipesh Padole (AIDS-48)
- Chaitanya Kewale (AIDS-44)

## 3) Problem Definition
Managing product/inventory data manually (registers, spreadsheets, or repeated editing of records) often leads to mistakes such as duplicate entries, wrong prices/quantities, and difficulty in tracking updates over time. A simple and reliable system is needed to store product details in a database and perform add/view/update/delete operations in a controlled way.

This project solves the problem by providing a CRUD (Create, Read, Update, Delete) application where the user can manage products using either a command-line interface or a web-based interface. All operations are stored in a MySQL database so that product data remains persistent and consistent across runs.

## 4) Project Description
This system is implemented using **Core Java** for the backend logic and **JDBC** for database connectivity with **MySQL**. It supports two ways to use the application:

- **CLI Mode:** `src/main/App.java` provides a menu-driven program for CRUD operations.
- **Web Mode:** `src/main/WebServer.java` runs a minimal Java HTTP server that serves the frontend and exposes API endpoints.

**How it works (Web Mode):**
1. The frontend UI (`web/index.html` + `web/index.css`) runs in the browser.
2. JavaScript sends HTTP requests to the Java server (`/api/products`).
3. The server processes requests and calls `ProductDAO`.
4. `ProductDAO` executes SQL using `PreparedStatement` via JDBC.
5. Data is stored/retrieved from **MySQL** (`ecommerce` database, `products` table), and the API returns JSON responses.

**Technologies used (as per this project):**
- Java (Core Java)
- JDBC (DriverManager, PreparedStatement)
- MySQL + MySQL Connector/J (`lib/mysql-connector-j-9.6.0.jar`)
- Java built-in HTTP server (`com.sun.net.httpserver.HttpServer`)
- HTML, CSS, JavaScript (frontend)

## 5) Features
- Add, view, update, and delete products (CRUD)
- Persistent storage using MySQL database
- Uses `PreparedStatement` for database operations (safer parameter handling)
- Web UI to manage products from the browser
- REST-style API endpoints:
  - `GET /api/products`
  - `GET /api/products/{id}`
  - `POST /api/products`
  - `PUT /api/products/{id}`
  - `DELETE /api/products/{id}`
- Menu-driven CLI interface for quick testing without a browser

## 6) Screenshots
- Add Screenshot Here (Web UI Home)
- Add Screenshot Here (Products Table)
- Add Screenshot Here (Add Product)
- Add Screenshot Here (Update Product)
- Add Screenshot Here (Delete Product / Status)

## 7) Database Connectivity Code
Actual database connection code used in this project (`src/util/DBConnection.java`):

```java
package util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Simple JDBC connection utility class.
 *
 * Change USER and PASSWORD as per your local MySQL setup.
 */
public class DBConnection {

    private static final String URL = "jdbc:mysql://localhost:3306/ecommerce";
    private static final String USER = "root";
    private static final String PASSWORD = "pass123";

    public static Connection getConnection() throws SQLException {
        // MySQL 8+ driver class (optional in modern JDBC, but kept for beginners).
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new SQLException("MySQL JDBC Driver not found. Add mysql-connector-j to /lib.", e);
        }

        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}
```

## 8) Evaluation Criteria
"Design of backend and front end is clear. DDL and DML operations are handled effectively and able to explain the application correctly" — Marks: 7

## 9) Conclusion
This project demonstrates a complete CRUD workflow using Core Java, JDBC, and MySQL, with both CLI and web-based interfaces. It cleanly separates frontend, backend handlers, and database access, and performs DDL/DML operations effectively through a structured DAO approach.

