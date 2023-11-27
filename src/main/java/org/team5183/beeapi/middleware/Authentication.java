package org.team5183.beeapi.middleware;

import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.google.gson.Gson;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.team5183.beeapi.authentication.JWTManager;
import org.team5183.beeapi.constants.Permission;
import org.team5183.beeapi.constants.Role;
import org.team5183.beeapi.entities.UserEntity;
import org.team5183.beeapi.response.BasicResponse;
import org.team5183.beeapi.response.ResponseStatus;
import spark.Filter;
import spark.Request;
import spark.Response;

import java.sql.SQLException;

import static spark.Spark.halt;

public class Authentication {
    private static final Logger logger = LogManager.getLogger(Authentication.class);

    private static final Gson gson = new Gson();

    public static void authenticate(Request request, Response response) {
        String token = request.headers("Authorization");
        if (token == null || token.isEmpty() || token.isBlank()) {
            response.header("WWW-Authenticate", "Bearer");
            halt(401, gson.toJson(new BasicResponse(ResponseStatus.ERROR, "Missing Authorization Header")));
        }

        if (!(token.startsWith("Bearer "))) {
            response.header("WWW-Authenticate", "Bearer");
            halt(401, gson.toJson(new BasicResponse(ResponseStatus.ERROR, "Invalid Token Format")));
        }

        token = token.replace("Bearer ", "");

        DecodedJWT jwt = null;

        try {
            jwt = JWTManager.decodeToken(token);
        } catch (JWTVerificationException e) {
            response.header("WWW-Authenticate", "Bearer error=\"invalid_token\"");
            halt(401, gson.toJson(new BasicResponse(ResponseStatus.ERROR, "Invalid Token")));
        }

        try {
            UserEntity user = UserEntity.getUserEntity(jwt.getClaim("id").asLong());

            if (user == null) {
                response.header("WWW-Authenticate", "Bearer error=\"invalid_token\"");
                halt(401, gson.toJson(new BasicResponse(ResponseStatus.ERROR, "Invalid Token")));
            }

            if (!user.getToken().equals(token)) {
                response.header("WWW-Authenticate", "Bearer error=\"invalid_token\"");
                halt(401, gson.toJson(new BasicResponse(ResponseStatus.ERROR, "Invalid Token")));
            }
        } catch (SQLException e) {
            halt(500, gson.toJson(new BasicResponse(ResponseStatus.ERROR, "Internal Server Error")));
        }
    }

    public static Filter checkPermission(Request request, Response response, Role role) {
        authenticate(request, response);

        String token = request.headers("Authorization").replace("Bearer ", "");

        try {
            UserEntity user = UserEntity.getUserEntityByToken(token);
            assert user != null;
            if (!(user.getRole().equals(role))) halt(403, gson.toJson(new BasicResponse(ResponseStatus.ERROR, "Insufficient Permissions")));
        } catch (SQLException e) {
            halt(500, gson.toJson(new BasicResponse(ResponseStatus.ERROR, "Internal Server Error")));
        }
        return null;
    }

    public static Filter checkPermission(Request request, Response response, Permission permission) {
        authenticate(request, response);

        String token = request.headers("Authorization").replace("Bearer ", "");

        try {
            UserEntity user = UserEntity.getUserEntityByToken(token);
            assert user != null;
            if (!(user.getPermissionsList().contains(permission)) && !((user.getRole().equals(Role.ADMIN)))) halt(403, gson.toJson(new BasicResponse(ResponseStatus.ERROR, "Insufficient Permissions")));
        } catch (SQLException e) {
            halt(500, gson.toJson(new BasicResponse(ResponseStatus.ERROR, "Internal Server Error")));
        }
        return null;
    }
}
