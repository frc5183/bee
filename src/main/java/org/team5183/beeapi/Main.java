package org.team5183.beeapi;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import spark.Service;

import static spark.Spark.*;

public class Main {
    public static final Logger logger = LogManager.getLogger(Main.class);

    public static void main(String[] args) {
        before("/*", (request, response) -> logger.info("Received " + request.requestMethod() + " request from " + request.ip() + " for " + request.pathInfo() + " with body \" " + request.body() + "\""));


        //todo get from config
        Service.ignite()
                .ipAddress("0.0.0.0")
                .port(5050);

    }
}