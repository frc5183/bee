package org.team5183.beeapi.endpoints;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import org.team5183.beeapi.constants.Permission;
import org.team5183.beeapi.constants.Role;
import org.team5183.beeapi.middleware.Authentication;
import org.team5183.beeapi.response.BasicResponse;
import org.team5183.beeapi.response.ResponseStatus;
import spark.Filter;
import spark.Request;
import spark.Response;

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

    void authenticate(Request request, Response response) {
        Authentication.authenticate(request, response);
    }

    Filter checkPermission(Request request, Response response, Role role) {
        return Authentication.checkPermission(request, response, role);
    }

    Filter checkPermission(Request request, Response response, Permission permission) {
        return Authentication.checkPermission(request, response, permission);
    }

    Filter checkBody(Request request, Response response) {
        if (request.body() == null || request.body().isEmpty() || request.body().isBlank())
            halt(400, new Gson().toJson(new BasicResponse(ResponseStatus.ERROR, "Missing Body")));
        return null;
    }

    Filter checkBody(Request request, Response response, Class<?> clazz) {
        checkBody(request, response);

        try {
            gson.fromJson(request.body(), clazz);
        } catch (JsonSyntaxException e) {
            halt(400, new Gson().toJson(new BasicResponse(ResponseStatus.ERROR, "Invalid Body")));
        }

        return null;
    }
}
