package org.team5183.beeapi.endpoints;

import com.google.gson.Gson;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.team5183.beeapi.response.BasicResponse;
import org.team5183.beeapi.response.ResponseStatus;

import static spark.Spark.*;

public class MiscEndpoint extends Endpoint {
    private static final Logger logger = LogManager.getLogger(MiscEndpoint.class);

    @Override
    public void registerEndpoints() {
        get("/", (request, response) -> {
            response.type("text/html");
            return "<h1>Bee API</h1><p>An API for the Bee Inventory Management System</p><p>Created by <a href=\"https://github.com/frc5183\">Team 5183</a></p>";
        });

        get("/status", (request, response) -> gson.toJson(new BasicResponse(ResponseStatus.SUCCESS, "OK")));

        before("/*", (request, response) -> {
            response.type("application/json");
            logger.info("Received " + request.requestMethod() + " request from " + request.ip() + " for " + request.url());
        });

        notFound((req, res) -> {
            res.status(404);
            return new Gson().toJson(new BasicResponse(ResponseStatus.ERROR, "Not Found"));
        });

        internalServerError((req, res) -> {
            logger.error("Internal server error.\nRequest IP = " + req.ip() + "\nRequest URL = " + req.url() + "\nRequest body = <censored>" + "\nRequest headers = <censored>" + "\nResponse body = <censored>");
            res.status(500);
            return gson.toJson(new BasicResponse(ResponseStatus.ERROR, "Internal server error"));
        });
    }
}
