package org.team5183.beeapi;

import com.google.gson.Gson;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.team5183.beeapi.endpoints.ItemEndpoint;
import org.team5183.beeapi.endpoints.MiscEndpoints;
import org.team5183.beeapi.endpoints.UserEndpoint;
import org.team5183.beeapi.entities.ItemEntity;
import org.team5183.beeapi.response.BasicResponse;
import org.team5183.beeapi.response.ResponseStatus;
import org.team5183.beeapi.util.Database;
import spark.Service;

import java.sql.SQLException;

import static spark.Spark.*;

public class Main {
    public static final Logger logger = LogManager.getLogger(Main.class);

    public static void main(String[] args) throws SQLException {
        ipAddress(System.getenv("IP") != null ? System.getenv("IP") : "localhost");
        port(System.getenv("PORT") != null ? Integer.parseInt(System.getenv("PORT")) : 5050);


        before("/*", (request, response) -> {
            response.type("application/json");
            logger.info("Received " + request.requestMethod() + " request from " + request.ip() + " for " + request.pathInfo() + " with body \" " + request.body() + "\"");
        });

        notFound((req, res) -> {
            res.status(404);
            return new Gson().toJson(new BasicResponse(ResponseStatus.ERROR, "Not Found"));
        });

        new ItemEndpoint();
        new UserEndpoint();
        new MiscEndpoints();

        Database.init();



        init();
    }
}