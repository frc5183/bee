package org.team5183.beeapi.endpoints;

import com.google.gson.JsonArray;
import org.team5183.beeapi.response.BasicResponse;
import org.team5183.beeapi.response.ResponseStatus;

import javax.persistence.Basic;

import static spark.Spark.*;

public class MiscEndpoints extends Endpoint {
    @Override
    void registerEndpoints() {
        get("/", (req, res) -> {
            res.type("text/html");
            return "<h1>Bee API</h1><p>An API for the Bee Inventory Management System</p><p>Created by <a href=\"https://github.com/frc5183\">Team 5183</a></p>";
        });
        get("/status", (req, res) -> {
            return gson.toJson(new BasicResponse(ResponseStatus.SUCCESS, "OK"));
        });
    }
}
