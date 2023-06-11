package org.team5183.beeapi;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.team5183.beeapi.constants.Role;
import org.team5183.beeapi.endpoints.ItemEndpoint;
import org.team5183.beeapi.endpoints.MiscEndpoint;
import org.team5183.beeapi.endpoints.UserEndpoint;
import org.team5183.beeapi.entities.UserEntity;
import org.team5183.beeapi.runnables.DatabaseRequestRunnable;
import org.team5183.beeapi.threading.ThreadingManager;

import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.sql.SQLException;
import java.util.Random;

import static spark.Spark.*;

public class Main {
    public static final Logger logger = LogManager.getLogger(Main.class);

    public static void main(String[] args) throws SQLException, IOException, NoSuchAlgorithmException, InvalidKeySpecException {
        // Check if JWT_SECRET is set, we need this, and it's not something we should randomly generate.
        if (System.getenv("JWT_SECRET") == null || System.getenv("JWT_SECRET").isEmpty() || System.getenv("JWT_SECRET").isBlank()) {
            logger.fatal("You must set JWT_SECRET environment variable for signing JWT tokens. This can be any random string however should be treated as a password (as in long and secure). \nIf you lose this it will just cause a minor inconvenience, all users tokens will be invalidated and they will have to log back in.");
            System.exit(1);
        }

        // Set port and IP address (environment variables IP & PORT)
        ipAddress(System.getenv("IP") != null ? System.getenv("IP") : "localhost");
        port(System.getenv("PORT") != null ? Integer.parseInt(System.getenv("PORT")) : 5050);


        // Register endpoints
        new ItemEndpoint().registerEndpoints();
        new UserEndpoint().registerEndpoints();
        new MiscEndpoint().registerEndpoints();

        // Add threading tasks
        ThreadingManager.addTask(new DatabaseRequestRunnable());

        // First run checks
        File init = new File("INITIALIZED");
        if (init.createNewFile()) {
            logger.warn("\n\n--------------------\nFirst run detected, creating a new admin user.");

            // Generate a random password.
            int leftLimit = 33; // !
            int rightLimit = 126; // ~
            Random random = new Random();
            StringBuilder buffer = new StringBuilder(14);
            for (int i = 0; i < 14; i++) {
                int randomLimitedInt = leftLimit + (int)
                        (random.nextFloat() * (rightLimit - leftLimit + 1));
                buffer.append((char) randomLimitedInt);
            }
            String password = buffer.toString();

            // Create a new admin user.
            new UserEntity("admin", password, "admin@example.com", "Admin", Role.ADMIN).update();

            // Print credentials to console.
            logger.warn("New account\nUsername: admin\nPassword: " + password + "\n--------------------\n\n");
        }
    }
}