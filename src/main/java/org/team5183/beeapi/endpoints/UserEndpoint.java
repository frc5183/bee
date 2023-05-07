package org.team5183.beeapi.endpoints;

import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.team5183.beeapi.authentication.HashPassword;
import org.team5183.beeapi.authentication.JWTManager;
import org.team5183.beeapi.constants.Role;
import org.team5183.beeapi.entities.UserEntity;
import org.team5183.beeapi.middleware.Authentication;
import org.team5183.beeapi.response.BasicResponse;
import org.team5183.beeapi.response.ResponseStatus;
import org.team5183.beeapi.util.Database;

import java.sql.SQLException;

import static spark.Spark.*;

public class UserEndpoint extends Endpoint {
    //todo
    private static final Logger logger = LogManager.getLogger(UserEndpoint.class);
    public UserEndpoint() {
        registerEndpoints();
    }
    @Override
    void registerEndpoints() {
        path("/users", () -> {
            post("/register", (req, res) -> {
                if (req.body() == null || req.body().isEmpty() || req.body().isBlank()) {
                    res.status(400);
                    return gson.toJson(new BasicResponse(ResponseStatus.ERROR, "Missing Body"));
                }

                JsonObject register = null;
                try {
                    register = gson.fromJson(req.body(), JsonObject.class);
                } catch (JsonSyntaxException e) {
                    res.status(400);
                    return gson.toJson(new BasicResponse(ResponseStatus.ERROR, "Invalid Body"));
                }

                if (register == null) {
                    res.status(400);
                    return gson.toJson(new BasicResponse(ResponseStatus.ERROR, "Invalid Body"));
                }

                String login = register.get("login").getAsString();
                String password = register.get("password").getAsString();
                String email = register.get("email").getAsString();
                String displayName = register.get("displayName").getAsString();

                if (login == null || login.isEmpty() || login.isBlank()) {
                    res.status(400);
                    return gson.toJson(new BasicResponse(ResponseStatus.ERROR, "Missing Username"));
                }

                if (password == null || password.isEmpty() || password.isBlank()) {
                    res.status(400);
                    return gson.toJson(new BasicResponse(ResponseStatus.ERROR, "Missing Password"));
                }

                if (email == null || email.isEmpty() || email.isBlank()) {
                    res.status(400);
                    return gson.toJson(new BasicResponse(ResponseStatus.ERROR, "Missing Email"));
                }

                if (!email.matches("^[a-zA-Z0-9.!#$%&’*+/=?^_`{|}~-]+@[a-zA-Z0-9-]+(?:\\.[a-zA-Z0-9-]+)*$")) {
                    res.status(400);
                    return gson.toJson(new BasicResponse(ResponseStatus.ERROR, "Invalid Email"));
                }

                if (displayName == null || displayName.isEmpty() || displayName.isBlank()) {
                    res.status(400);
                    return gson.toJson(new BasicResponse(ResponseStatus.ERROR, "Missing Display Name"));
                }

                try {
                    if (Database.getUserEntityByLogin(login) != null) {
                        res.status(400);
                        return gson.toJson(new BasicResponse(ResponseStatus.ERROR, "A user with this login already exists"));
                    }

                    if (Database.getUserEntityByEmail(email) != null) {
                        res.status(400);
                        return gson.toJson(new BasicResponse(ResponseStatus.ERROR, "A user with this email already exists"));
                    }
                } catch (SQLException e) {
                    logger.error(e);
                    res.status(500);
                    return gson.toJson(new BasicResponse(ResponseStatus.ERROR, "Internal Server Error"));
                }

                String token = JWTManager.generateToken();
                UserEntity ue = new UserEntity(login, password, token, email, displayName, Role.USER);

                try {
                    Database.upsertUserEntity(ue);
                } catch (SQLException e) {
                    logger.error(e);
                    res.status(500);
                    return gson.toJson(new BasicResponse(ResponseStatus.ERROR, "Internal Server Error"));
                }

                return gson.toJson(new BasicResponse(ResponseStatus.SUCCESS, "Created new user with ID " + ue.getId(), ue.toJson()));
            });

            post("/login", (req, res) -> {
                if (req.body() == null || req.body().isEmpty() || req.body().isBlank()) {
                    res.status(400);
                    return gson.toJson(new BasicResponse(ResponseStatus.ERROR, "Missing Body"));
                }

                JsonObject login = null;
                try {
                    login = gson.fromJson(req.body(), JsonObject.class);
                } catch (JsonSyntaxException e) {
                    res.status(400);
                    return gson.toJson(new BasicResponse(ResponseStatus.ERROR, "Invalid Body"));
                }

                if (login == null) {
                    res.status(400);
                    return gson.toJson(new BasicResponse(ResponseStatus.ERROR, "Invalid Body"));
                }

//                if ((!(login.has("login")) && !(login.has("email")))
//                        || (!(login.get("login").getAsString().isBlank()) && !(login.get("email").getAsString().isBlank()))
//                        || (!(login.get("login").getAsString().isEmpty()) && !(login.get("email").getAsString().isEmpty()))
//                ) {
//                    res.status(400);
//                    return gson.toJson(new BasicResponse(ResponseStatus.ERROR, "Missing Login or Email"));
//                }

                if (!login.has("password") || login.get("password").getAsString().isEmpty() || login.get("password").getAsString().isBlank()) {
                    res.status(400);
                    return gson.toJson(new BasicResponse(ResponseStatus.ERROR, "Missing Password"));
                }

                UserEntity ue = null;
                try {
                    if (login.has("login")) {
                        ue = Database.getUserEntityByLogin(login.get("login").getAsString());
                    } else if (login.has("email")) {
                        ue = Database.getUserEntityByEmail(login.get("email").getAsString());
                    }
                } catch (SQLException e) {
                    logger.error(e);
                    res.status(500);
                    return gson.toJson(new BasicResponse(ResponseStatus.ERROR, "Internal Server Error"));
                }

                if (ue == null) {
                    res.status(400);
                    return gson.toJson(new BasicResponse(ResponseStatus.ERROR, "Invalid Login or Email"));
                }

                if (!HashPassword.checkPassword(login.get("password").getAsString(), ue.getSalt(), ue.getHashedPassword())) {
                    res.status(400);
                    return gson.toJson(new BasicResponse(ResponseStatus.ERROR, "Incorrect Password"));
                }

                res.header("Authorization", "Bearer " + ue.getToken());
                if (ue.getToken().isEmpty() || ue.getToken().isBlank()) ue.setToken(JWTManager.generateToken());
                return gson.toJson(new BasicResponse(ResponseStatus.SUCCESS, "Logged in successfully", ue.toJson()));
            });

            path("/me", () -> {
                before(Authentication::authenticate);

                get("", (req, res) -> {
                    String token = req.headers("Authorization").replace("Bearer ", "");

                    UserEntity ue;
                    try {
                        ue = Database.getUserEntityByToken(token);
                    } catch (SQLException e) {
                        logger.error(e);
                        res.status(500);
                        return gson.toJson(new BasicResponse(ResponseStatus.ERROR, "Internal Server Error"));
                    }

                    if (ue == null) {
                        res.status(400);
                        return gson.toJson(new BasicResponse(ResponseStatus.ERROR, "Invalid Token"));
                    }


                    return gson.toJson(new BasicResponse(ResponseStatus.SUCCESS, "User with id " + ue.getId(), ue.toJson()));
                });

                patch("/changePassword", (req, res) -> {
                    if (req.body() == null || req.body().isEmpty() || req.body().isBlank()) {
                        res.status(400);
                        return gson.toJson(new BasicResponse(ResponseStatus.ERROR, "Missing Body"));
                    }

                    JsonObject changePassword = null;
                    try {
                        changePassword = gson.fromJson(req.body(), JsonObject.class);
                    } catch (JsonSyntaxException e) {
                        res.status(400);
                        return gson.toJson(new BasicResponse(ResponseStatus.ERROR, "Invalid Body"));
                    }

                    if (changePassword == null) {
                        res.status(400);
                        return gson.toJson(new BasicResponse(ResponseStatus.ERROR, "Invalid Body"));
                    }

                    if (!changePassword.has("oldPassword") || changePassword.get("oldPassword").getAsString().isBlank() || changePassword.get("oldPassword").getAsString().isEmpty()) {
                        res.status(400);
                        return gson.toJson(new BasicResponse(ResponseStatus.ERROR, "Missing Old Password"));
                    }

                    if (!changePassword.has("newPassword") || changePassword.get("newPassword").getAsString().isBlank() || changePassword.get("newPassword").getAsString().isEmpty()) {
                        res.status(400);
                        return gson.toJson(new BasicResponse(ResponseStatus.ERROR, "Missing New Password"));
                    }

                    String token = req.headers("Authorization");
                    token = token.replace("Bearer ", "");

                    UserEntity ue = null;
                    try {
                        ue = Database.getUserEntityByToken(token);
                    } catch (SQLException e) {
                        logger.error(e);
                        res.status(500);
                        return gson.toJson(new BasicResponse(ResponseStatus.ERROR, "Internal Server Error"));
                    }

                    if (ue == null) {
                        res.status(400);
                        return gson.toJson(new BasicResponse(ResponseStatus.ERROR, "Invalid Token"));
                    }

                    if (!HashPassword.checkPassword(changePassword.get("oldPassword").getAsString(), ue.getSalt(), ue.getHashedPassword())) {
                        res.status(400);
                        return gson.toJson(new BasicResponse(ResponseStatus.ERROR, "Incorrect Password"));
                    }

                    String newPassword = changePassword.get("newPassword").getAsString();
                    String[] hashed = HashPassword.generateSaltedHashedPassword(newPassword);


                    try {
                        Database.upsertUserEntity(ue);
                    } catch (SQLException e) {
                        logger.error(e);
                        res.status(500);
                        return gson.toJson(new BasicResponse(ResponseStatus.ERROR, "Internal Server Error"));
                    }

                    return gson.toJson(new BasicResponse(ResponseStatus.SUCCESS, "Password Changed Successfully"));
                });

                patch("/update", (req, res) -> {
                    if (req.body() == null || req.body().isEmpty() || req.body().isBlank()) {
                        res.status(400);
                        return gson.toJson(new BasicResponse(ResponseStatus.ERROR, "Missing Body"));
                    }

                    UserEntity update = null;
                    try {
                        update = gson.fromJson(req.body(), UserEntity.class);
                    } catch (JsonSyntaxException e) {
                        res.status(400);
                        return gson.toJson(new BasicResponse(ResponseStatus.ERROR, "Invalid Body"));
                    }

                    if (update == null) {
                        res.status(400);
                        return gson.toJson(new BasicResponse(ResponseStatus.ERROR, "Invalid Body"));
                    }

                    if (!update.getEmail().matches("^[a-zA-Z0-9.!#$%&’*+/=?^_`{|}~-]+@[a-zA-Z0-9-]+(?:\\.[a-zA-Z0-9-]+)*$")) {
                        res.status(400);
                        return gson.toJson(new BasicResponse(ResponseStatus.ERROR, "Invalid Email"));
                    }

                    String token = req.headers("Authorization");
                    token = token.replace("Bearer ", "");

                    UserEntity ue = null;
                    try {
                        ue = Database.getUserEntityByToken(token);
                    } catch (SQLException e) {
                        logger.error(e);
                        res.status(500);
                        return gson.toJson(new BasicResponse(ResponseStatus.ERROR, "Internal Server Error"));
                    }

                    assert ue != null;

                    ue.setLogin(update.getLogin());
                    ue.setEmail(update.getEmail());
                    ue.setDisplayName(update.getDisplayName());

                    try {
                        Database.updateUserEntity(ue);
                    } catch (SQLException e) {
                        logger.error(e);
                        res.status(500);
                        return gson.toJson(new BasicResponse(ResponseStatus.ERROR, "Internal Server Error"));
                    }

                    return gson.toJson(new BasicResponse(ResponseStatus.SUCCESS, "Updated User with ID " + ue.getId(), ue.toJson()));
                });
                patch("/invalidatetoken", (req, res) -> {
                    String token = req.headers("Authorization");
                    token = token.replace("Bearer ", "");

                    UserEntity ue = null;
                    try {
                        ue = Database.getUserEntityByToken(token);
                    } catch (SQLException e) {
                        logger.error(e);
                        res.status(500);
                        return gson.toJson(new BasicResponse(ResponseStatus.ERROR, "Internal Server Error"));
                    }

                    assert ue != null;

                    ue.setToken("");

                    res.header("Authorization", "");
                    return gson.toJson(new BasicResponse(ResponseStatus.SUCCESS, "Invalidated token " + token));
                });
            });

            put("/logout", (req, res) -> {
                before(Authentication::authenticate);
                res.header("Authorization", "");
                return gson.toJson(new BasicResponse(ResponseStatus.SUCCESS, "Logged out successfully"));
            });
        });
    }
}
