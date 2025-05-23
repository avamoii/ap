package org.example.DAO;

import java.sql.Connection;
import java.sql.DriverManager;

public class DatabaseConnectionManager {
    // Database connection details
    private static final String DB_URL = "jdbc:postgres://localhost:3306/your_database";
    private static final String USER = "your_username";
    private static final String PASSWORD = "your_password";
    private static Connection connection;
    private static DatabaseConnectionManager instance;

    // Private constructor to prevent instantiation
    private DatabaseConnectionManager() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            connection = DriverManager.getConnection(DB_URL, USER, PASSWORD);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Singleton pattern to get the instance of the connection manager
    public static DatabaseConnectionManager getInstance() {
        if (instance == null) {
            instance = new DatabaseConnectionManager();
        }
        return instance;
    }

    // Method to get the connection
    public Connection getConnection() {
        return connection;
    }

    // Method to close the connection
    public void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (Exception e) {
            e.printStackTrace();

        }
    }
}

