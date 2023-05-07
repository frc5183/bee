package org.team5183.beeapi.endpoints;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.team5183.beeapi.response.BasicResponse;
import org.team5183.beeapi.response.ResponseStatus;

import static spark.Spark.*;

public class MiscEndpoints extends Endpoint {
    private static final Logger logger = LogManager.getLogger(MiscEndpoints.class);
    @Override
    void registerEndpoints() {
        get("/", (req, res) -> {
            res.type("text/html");
            return "<h1>Bee API</h1><p>An API for the Bee Inventory Management System</p><p>Created by <a href=\"https://github.com/frc5183\">Team 5183</a></p>";
        });
        get("/status", (req, res) -> gson.toJson(new BasicResponse(ResponseStatus.SUCCESS, "OK")));
    }
}
