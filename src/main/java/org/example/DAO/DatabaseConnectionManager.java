package org.example.DAO;

import java.sql.Connection;
import java.sql.DriverManager;
import io.github.cdimascio.dotenv.Dotenv;

public class DatabaseConnectionManager {
    private static final Dotenv dotenv = Dotenv.load();
    private static final String DB_URL = "jdbc:postgresql://localhost:" +
            dotenv.get("DATABASE_PORT") + "/" + dotenv.get("DATABASE_NAME");
    private static final String USER = dotenv.get("DATABASE_USER");
    private static final String PASSWORD = dotenv.get("DATABASE_PASSWORD");
    private static Connection connection;
    private static DatabaseConnectionManager instance;

    private DatabaseConnectionManager() {
        try {
            Class.forName("org.postgresql.Driver");
            connection = DriverManager.getConnection(DB_URL, USER, PASSWORD);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static DatabaseConnectionManager getInstance() {
        if (instance == null) {
            instance = new DatabaseConnectionManager();
        }
        return instance;
    }

    public Connection getConnection() {
        return connection;
    }

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
