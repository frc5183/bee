package org.team5183.beeapi.endpoints;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonSyntaxException;
import org.team5183.beeapi.constants.Permission;
import org.team5183.beeapi.constants.Role;
import org.team5183.beeapi.middleware.Authentication;
import org.team5183.beeapi.response.BasicResponse;
import org.team5183.beeapi.response.ResponseStatus;
import spark.Filter;
import spark.Request;
import spark.Response;
import spark.Spark;

import static spark.Spark.halt;

public abstract class Endpoint {
    static final Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();

    /**
     * Creates a new endpoint and registers all endpoints for the given endpoint.
     */
    public Endpoint() {
        registerEndpoints();
    }

    /**
     * Registers all endpoints for the given endpoint.
     */
    abstract void registerEndpoints();

    /**
     * @param request The request to authenticate
     * @param response The response to respond with
     * @see Authentication#authenticate(Request, Response)
     */
    void authenticate(Request request, Response response) {
        Authentication.authenticate(request, response);
    }

    /**
     * Checks if the user has the given role, responds with a 403 error if this check fails.
     * @param request The request to check
     * @param response The response to respond with
     * @param role The role to check
     * @return null
     */
    Filter checkPermission(Request request, Response response, Role role) {
        return Authentication.checkPermission(request, response, role);
    }

    /**
     * Checks if the user has the given permission, responds with a 403 error if this check fails.
     * @param request The request to check
     * @param response The response to respond with
     * @param permission The permission to check
     * @return null
     */
    Filter checkPermission(Request request, Response response, Permission permission) {
        return Authentication.checkPermission(request, response, permission);
    }


    /**
     * Checks if the body isn't empty, responds with a 400 error if this check fails.
     * @param request The request to check
     * @param response The response to respond with (unused, just forced by Spark)
     */
    void isBodyEmpty(Request request, Response response) {
        if (request.body() == null || request.body().isEmpty() || request.body().isBlank())
            end(400, ResponseStatus.ERROR, "Missing Body");
    }

    /**
     * Checks if the body isn't empty and if it can be turned into the specified class, responds with a 400 error if these checks fail.
     * @param request The request to check
     * @param response The response to respond with
     * @param clazz The class to check the body against
     */
    void checkBody(Request request, Response response, Class<?> clazz) {
        isBodyEmpty(request, response);
        try {
            gson.fromJson(request.body(), clazz);
        } catch (JsonSyntaxException e) {
            end(400, ResponseStatus.ERROR, "Invalid Body");
        }
    }

    /**
     * Halts the request with the given response, status, and response status. Should only be used for errors or unintended responses.
     * @param status The status to respond with
     * @param responseStatus The response status to respond with
     * @see Spark#halt(int, String)
     * @see BasicResponse#BasicResponse(ResponseStatus) 
     * @see ResponseStatus
     */
    void end(int status, ResponseStatus responseStatus) {
        halt(status, gson.toJson(new BasicResponse(responseStatus)));
    }


    /**
     * Halts the request with the given response, status, response status, and message. Should only be used for errors or unintended responses.
     * @param status The status to respond with
     * @param responseStatus The response status to respond with
     * @param message The message to respond with
     * @see Spark#halt(int, String)
     * @see BasicResponse#BasicResponse(ResponseStatus, String)
     * @see ResponseStatus
     */
    void end(int status, ResponseStatus responseStatus, String message) {
        halt(status, gson.toJson(new BasicResponse(responseStatus, message)));
    }


    /**
     * Halts the request with the given response, status, response status, and data. Should only be used for errors or unintended responses.
     * @param status The status to respond with
     * @param responseStatus The response status to respond with
     * @param data The data to respond with
     * @see Spark#halt(int, String)
     * @see BasicResponse#BasicResponse(ResponseStatus, JsonElement)
     * @see ResponseStatus
     */
    void end(int status, ResponseStatus responseStatus, JsonElement data) {
        halt(status, gson.toJson(new BasicResponse(responseStatus, data)));
    }

    /**
     * Halts the request with the given response, status, response status, message, and data. Should only be used for errors or unintended responses.
     * @param status The status to respond with
     * @param responseStatus The response status to respond with
     * @param message The message to respond with
     * @param data The data to respond with
     * @see Spark#halt(int, String)
     * @see BasicResponse#BasicResponse(ResponseStatus, String, JsonElement)
     * @see ResponseStatus
     */
    void end(int status, ResponseStatus responseStatus, String message, JsonElement data) {
        halt(status, gson.toJson(new BasicResponse(responseStatus, message, data)));
    }

    /**
     * Return the token from the request.
     * @param request The request to get the token from
     * @return The token from the request without the "Bearer " prefix
     */
    String getToken(Request request) {
        if (request.headers("Authorization") == null || request.headers("Authorization").isEmpty() || request.headers("Authorization").isBlank()) {
            this.end(401, ResponseStatus.ERROR, "Missing Token");
        }

        if (!(request.headers("Authorization").startsWith("Bearer "))) {
            this.end(401, ResponseStatus.ERROR, "Invalid Token");
        }

        return request.headers("Authorization").replace("Bearer ", "");
    }
}
