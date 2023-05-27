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
                UserEntity user = new UserEntity(login, password, token, email, displayName, Role.USER);

                try {
                    Database.upsertUserEntity(user);
                } catch (SQLException e) {
                    logger.error(e);
                    res.status(500);
                    return gson.toJson(new BasicResponse(ResponseStatus.ERROR, "Internal Server Error"));
                }

                return gson.toJson(new BasicResponse(ResponseStatus.SUCCESS, "Created new user with ID " + user.getId(), gson.toJsonTree(user)));
            });

            post("/login", (req, res) -> {
                JsonObject login = jsonFromBody(req);
                assert login != null;

                if (login.has("login") && login.has("email")) {
                    res.status(400);
                    return gson.toJson(new BasicResponse(ResponseStatus.ERROR, "Cannot have both login and email"));
                }

                if (!login.has("login") && !login.has("email")) {
                    res.status(400);
                    return gson.toJson(new BasicResponse(ResponseStatus.ERROR, "Missing Login or Email"));
                }

                if (login.has("login") && (login.get("login").getAsString().isEmpty() || login.get("login").getAsString().isBlank())) {
                    res.status(400);
                    return gson.toJson(new BasicResponse(ResponseStatus.ERROR, "Login is empty."));
                }

                if (login.has("email") && (login.get("email").getAsString().isEmpty() || login.get("email").getAsString().isBlank())) {
                    res.status(400);
                    return gson.toJson(new BasicResponse(ResponseStatus.ERROR, "Email is empty."));
                }

                if (!login.has("password") || login.get("password").getAsString().isEmpty() || login.get("password").getAsString().isBlank()) {
                    res.status(400);
                    return gson.toJson(new BasicResponse(ResponseStatus.ERROR, "Missing Password"));
                }

                UserEntity user = null;
                try {
                    if (login.has("login")) {
                        user = Database.getUserEntityByLogin(login.get("login").getAsString());
                    } else if (login.has("email")) {
                        user = Database.getUserEntityByEmail(login.get("email").getAsString());
                    }
                } catch (SQLException e) {
                    logger.error(e);
                    res.status(500);
                    return gson.toJson(new BasicResponse(ResponseStatus.ERROR, "Internal Server Error"));
                }

                if (user == null) {
                    res.status(400);
                    return gson.toJson(new BasicResponse(ResponseStatus.ERROR, "Invalid Login or Email"));
                }

                if (!HashPassword.checkPassword(login.get("password").getAsString(), user.getSalt(), user.getHashedPassword())) {
                    res.status(400);
                    return gson.toJson(new BasicResponse(ResponseStatus.ERROR, "Incorrect Password"));
                }

                res.header("Authorization", "Bearer " + user.getToken());
                if (user.getToken().isEmpty() || user.getToken().isBlank()) user.setToken(JWTManager.generateToken());
                return gson.toJson(new BasicResponse(ResponseStatus.SUCCESS, "Logged in successfully", gson.toJsonTree(user)));
            });

            path("/me", () -> {
                before(Authentication::authenticate);

                get("", (req, res) -> {
                    String token = req.headers("Authorization").replace("Bearer ", "");

                    UserEntity user;
                    try {
                        user = Database.getUserEntityByToken(token);
                    } catch (SQLException e) {
                        logger.error(e);
                        res.status(500);
                        return gson.toJson(new BasicResponse(ResponseStatus.ERROR, "Internal Server Error"));
                    }

                    if (user == null) end(400, ResponseStatus.ERROR, "Invalid Token");


                    return gson.toJson(new BasicResponse(ResponseStatus.SUCCESS, "User with id " + user.getId(), gson.toJsonTree(user)));
                });

                patch("/changePassword", (req, res) -> {

                    JsonObject changePassword = null;
                    try {
                        changePassword = gson.fromJson(req.body(), JsonObject.class);
                    } catch (JsonSyntaxException e) {
                        end(400, ResponseStatus.ERROR, "Invalid Body");
                    }

                    if (changePassword == null) end(400, ResponseStatus.ERROR, "Invalid Body");


                    assert changePassword != null;

                    if (!changePassword.has("oldPassword") || changePassword.get("oldPassword").getAsString().isEmpty() || changePassword.get("oldPassword").getAsString().isBlank()) end(400, ResponseStatus.ERROR, "Missing Old Password");


                    if (!changePassword.has("newPassword") || changePassword.get("newPassword").getAsString().isEmpty() || changePassword.get("newPassword").getAsString().isBlank()) end(400, ResponseStatus.ERROR, "Missing New Password");



                    String token = getToken(req);

                    UserEntity ue = null;
                    try {
                        ue = Database.getUserEntityByToken(token);
                    } catch (SQLException e) {
                        logger.error(e);
                        end(500, ResponseStatus.ERROR, "Internal Server Error");
                    }

                    if (ue == null) end(400, ResponseStatus.ERROR, "Invalid Token");

                    assert ue != null;
                    if (!HashPassword.checkPassword(changePassword.get("oldPassword").getAsString(), ue.getSalt(), ue.getHashedPassword())) end(400, ResponseStatus.ERROR, "Incorrect Password");


                    String newPassword = changePassword.get("newPassword").getAsString();
                    byte[][] hashed = HashPassword.generateSaltedHashedPassword(newPassword);
                    ue.setSalt(hashed[0]);
                    ue.setHashedPassword(hashed[1]);


                    try {
                        Database.upsertUserEntity(ue);
                    } catch (SQLException e) {
                        logger.error(e);
                        end(500, ResponseStatus.ERROR, "Internal Server Error");
                    }

                    return gson.toJson(new BasicResponse(ResponseStatus.SUCCESS, "Password Changed Successfully"));
                });

                patch("/update", (req, res) -> {

                    JsonObject update = null;
                    try {
                        update = gson.fromJson(req.body(), JsonObject.class);
                    } catch (JsonSyntaxException e) {
                        end(400, ResponseStatus.ERROR, "Invalid Body");
                    }

                    if (update == null)
                        end(400, ResponseStatus.ERROR, "Invalid Body");

                    assert update != null;

                    if (
                            (update.has("login") && (update.get("login").getAsString().isEmpty() || update.get("login").getAsString().isBlank()))
                            && (update.has("email") && (update.get("email").getAsString().isEmpty() || update.get("email").getAsString().isBlank()))
                            && (update.has("displayName") && (update.get("displayName").getAsString().isEmpty() || update.get("displayName").getAsString().isBlank()))
                    )
                        end(400, ResponseStatus.ERROR, "Missing one or more: login, email, displayName");


                    if (!update.get("email").getAsString().matches("^[a-zA-Z0-9.!#$%&’*+/=?^_`{|}~-]+@[a-zA-Z0-9-]+(?:\\.[a-zA-Z0-9-]+)*$"))
                        end(400, ResponseStatus.ERROR, "Invalid Email");


                    String token = getToken(req);

                    UserEntity user;
                    try {
                        user = Database.getUserEntityByToken(token);
                    } catch (SQLException e) {
                        logger.error(e);
                        res.status(500);
                        return gson.toJson(new BasicResponse(ResponseStatus.ERROR, "Internal Server Error"));
                    }

                    assert user != null;

                    if (update.has("login") && !(update.get("login").getAsString().isEmpty() || update.get("login").getAsString().isBlank())) {
                        if (Database.getUserEntityByLogin(update.get("login").getAsString()) != null) {
                            end(400, ResponseStatus.ERROR, "Login already taken");
                        } else {
                            user.setLogin(update.get("login").getAsString());
                        }
                    }

                    if (update.has("email") && !(update.get("email").getAsString().isEmpty() || update.get("email").getAsString().isBlank())) {
                        if (Database.getUserEntityByEmail(update.get("email").getAsString()) != null) {
                            end(400, ResponseStatus.ERROR, "Email already taken");
                        } else {
                            user.setEmail(update.get("email").getAsString());
                        }
                    }

                    if (update.has("displayName") && !(update.get("displayName").getAsString().isEmpty() || update.get("displayName").getAsString().isBlank()))
                        user.setDisplayName(update.get("displayName").getAsString());

                    try {
                        Database.updateUserEntity(user);
                    } catch (SQLException e) {
                        logger.error(e);
                        res.status(500);
                        return gson.toJson(new BasicResponse(ResponseStatus.ERROR, "Internal Server Error"));
                    }

                    return gson.toJson(new BasicResponse(ResponseStatus.SUCCESS, "Updated User with ID " + user.getId(), gson.toJsonTree(user)));
                });
                patch("/invalidatetoken", (req, res) -> {
                    String token = getToken(req);

                    UserEntity ue;
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
