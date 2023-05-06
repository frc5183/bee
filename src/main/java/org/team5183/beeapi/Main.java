package org.team5183.beeapi;

import com.google.gson.Gson;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.team5183.beeapi.endpoints.ItemEndpoint;
import org.team5183.beeapi.entities.ItemEntity;
import org.team5183.beeapi.util.Database;
import spark.Service;

import java.sql.SQLException;

import static spark.Spark.*;

public class Main {
    public static final Logger logger = LogManager.getLogger(Main.class);

    public static void main(String[] args) throws SQLException {
        before("/*", (request, response) -> logger.info("Received " + request.requestMethod() + " request from " + request.ip() + " for " + request.pathInfo() + " with body \" " + request.body() + "\""));
        new ItemEndpoint();
        Database.init();


        //todo get from config
        Service.ignite()
                .ipAddress("0.0.0.0")
                .port(5050);

        logger.info(new Gson().toJson(new ItemEntity("asd", "asd", "asd", 1.0D, null, null)));

    }
}