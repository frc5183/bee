package org.team5183.beeapi;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.team5183.beeapi.constants.Role;
import org.team5183.beeapi.endpoints.ItemEndpoint;
import org.team5183.beeapi.endpoints.MiscEndpoint;
import org.team5183.beeapi.endpoints.UserEndpoint;
import org.team5183.beeapi.entities.UserEntity;
import org.team5183.beeapi.runnables.DatabaseRunnable;
import org.team5183.beeapi.runnables.NamedRunnable;
import org.team5183.beeapi.threading.ThreadingManager;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Random;

import static spark.Spark.ipAddress;
import static spark.Spark.port;

public class Main {
    private static final Logger logger = LogManager.getLogger(Main.class);

    public static void main(String[] args) throws IOException {
        // Check if JWT_SECRET is set, we need this, and it's not something we should randomly generate.
//        if (System.getenv("JWT_SECRET") == null || System.getenv("JWT_SECRET").isEmpty() || System.getenv("JWT_SECRET").isBlank()) {
//            logger.fatal("You must set JWT_SECRET environment variable for signing JWT tokens. This can be any random string however should be treated as a password (as in long and secure). \nIf you lose this it will just cause a minor inconvenience, all users tokens will be invalidated and they will have to log back in.");
//            System.exit(1);
//        }
        ConfigurationParser.parseConfiguration("config.json");

        // Set port and IP address (environment variables IP & PORT)
        ipAddress(System.getenv("IP") != null ? System.getenv("IP") : "localhost");
        port(System.getenv("PORT") != null ? Integer.parseInt(System.getenv("PORT")) : 5050);

        Thread tm = new Thread(new ThreadingManager());
        tm.setName("Threading Manager");
        tm.start();

        // Add threading tasks
        ThreadingManager.addTask(new DatabaseRunnable());


        while (DatabaseRunnable.getReady().isDone()) {}

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            logger.info("Shutting down...");
            ThreadingManager.shutdown();

            while (ThreadingManager.getStatus() != NamedRunnable.RunnableStatus.ENDED) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            logger.info("Shutdown complete.");
            System.exit(0);
        }));


        // Register endpoints
        new ItemEndpoint().registerEndpoints();
        new UserEndpoint().registerEndpoints();
        new MiscEndpoint().registerEndpoints();


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

            try {
                // Create a new admin user.
                new UserEntity("admin", password, "admin@example.com", "Admin", Role.ADMIN).create();
            } catch (Exception e) {
                logger.fatal("Failed to create admin user, exiting.");
                init.delete();
                System.exit(1);
            }

            // Print credentials to console.
            logger.warn("New account\nUsername: admin\nPassword: " + password + "\n--------------------\n\n");
            if (init.canWrite()) {
                new FileWriter("INITIALIZED").write("!! IF YOU DELETE THIS IT WILL RECREATE THE admin USER !!\nWHICH MAY CAUSE ERRORS WITH DATABASE UNIQUE CONSTRAINTS.\nDELETE THE DATABASE OR ENSURE THAT THE admin USER WITH EMAIL admin@example.com DOES NOT EXIST BEFORE DELETING");
                init.setReadOnly();
            }
        }
    }
}