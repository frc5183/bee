package org.team5183.beeapi.endpoints;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import static spark.Spark.*;

public class UserEndpoint implements Endpoint {
    private static final Logger logger = LogManager.getLogger(UserEndpoint.class);
    public UserEndpoint() {
        registerEndpoints();
    }
    @Override
    public void registerEndpoints() {
        path("/users", () -> {
            get("", (req, res) -> {
                return "Hello, world!";
            });
        });
    }
}
