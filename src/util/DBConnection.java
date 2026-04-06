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
