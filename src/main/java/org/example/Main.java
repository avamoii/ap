package org.example;
import static spark.Spark.*;
import org.example.config.HibernateUtil;
import org.hibernate.Session;
import java.util.logging.LogManager;
import org.example.controller.UserController;


public class Main {
    public static void main(String[] args) {
        port(8080);
        LogManager.getLogManager().reset(); // disable logging spam from Hibernate

        UserController.initRoutes();
    }
}
