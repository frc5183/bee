package org.team5183.beeapi;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.team5183.beeapi.endpoints.ItemEndpoint;
import org.team5183.beeapi.endpoints.MiscEndpoint;
import org.team5183.beeapi.endpoints.UserEndpoint;
import org.team5183.beeapi.util.Database;

import java.sql.SQLException;

import static spark.Spark.*;

public class Main {
    public static final Logger logger = LogManager.getLogger(Main.class);

    public static void main(String[] args) throws SQLException {
        if (System.getenv("JWT_SECRET") == null || System.getenv("JWT_SECRET").isEmpty() || System.getenv("JWT_SECRET").isBlank()) {
            logger.fatal("You must set JWT_SECRET environment variable for signing JWT tokens. This can be any random string however should be treated as a password (as in long and secure). If you lose this, all users will be logged out and will have to re-login.");
            System.exit(1);
        }

        ipAddress(System.getenv("IP") != null ? System.getenv("IP") : "localhost");
        port(System.getenv("PORT") != null ? Integer.parseInt(System.getenv("PORT")) : 5050);

        new ItemEndpoint();
        new UserEndpoint();
        new MiscEndpoint();

        Database.init();
    }
}