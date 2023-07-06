package org.team5183.beeapi.endpoints;

import com.google.gson.JsonObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.team5183.beeapi.authentication.HashPassword;
import org.team5183.beeapi.authentication.JWTManager;
import org.team5183.beeapi.constants.Role;
import org.team5183.beeapi.entities.UserEntity;
import org.team5183.beeapi.middleware.Authentication;
import org.team5183.beeapi.response.BasicResponse;
import org.team5183.beeapi.response.ResponseStatus;
import spark.Request;
import spark.Response;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static spark.Spark.*;

public class UserEndpoint extends Endpoint {
    private static final Logger logger = LogManager.getLogger(UserEndpoint.class);

    @Override
    public void registerEndpoints() {
        //todo allow admin to get all users and basically call /me on all users just w/o the token and its just an id instead of /me
        path("/users", () -> {
            post("/register", this::registerUser);

            post("/login", this::loginUser);

            get("/all", this::getAllUsers);

            path("/:id", () -> {
                //todo fix this lmao
//                before(Authentication::authenticate);
//                before("", (req,res) -> Authentication.checkPermission(req, res, Role.ADMIN));

                get("", this::getSelfUser);

                patch("/overwritePassword", this::changeSelfPassword);

                patch("", this::updateUser);
            });

            path("/me", () -> {
                before(Authentication::authenticate);

                get("", this::getSelfUser);

                patch("/changePassword", this::changeSelfPassword);

                patch("/update", this::updateUser);

//                delete("", this::deleteSelf);
            });

            patch("/invalidateTokens", this::invalidateTokens);

            post("/logout", this::logoutSelf);
        });
    }

    private UserEntity getUserByToken(Request request, Response response) {
        String token = getToken(request);

        UserEntity user = null;
        try {
            user = UserEntity.getUserEntityByToken(token);
        } catch (Exception e) {
            end(500, ResponseStatus.ERROR, "Internal Server Error");
        }

        return user;
    }

    private UserEntity getUserByLogin(Request req, Response res) {
        JsonObject json = this.jsonFromBody(req);

        String login = json.get("login").getAsString();
        if (login == null || login.isEmpty() || login.isBlank()) {
            end(400, ResponseStatus.ERROR, "Missing login");
        }

        UserEntity user = null;
        try {
            user = UserEntity.getUserEntityByLogin(login);
        } catch (SQLException e) {
            e.printStackTrace();
            end(500, ResponseStatus.ERROR, "Internal Server Error");
        }
        return user;
    }

    private UserEntity getUserByEmail(Request req, Response res) {
        JsonObject json = this.jsonFromBody(req);

        String email = json.get("email").getAsString();
        if (email == null || email.isEmpty() || email.isBlank()) {
            end(400, ResponseStatus.ERROR, "Missing email");
        }

        UserEntity user = null;
        try {
            user = UserEntity.getUserEntityByEmail(email);
        } catch (SQLException e) {
            e.printStackTrace();
            end(500, ResponseStatus.ERROR, "Internal Server Error");
        }
        return user;
    }

    private String registerUser(Request req, Response res) {
        JsonObject register = this.jsonFromBody(req);

        if (register.get("login") == null) end(400, ResponseStatus.ERROR, "Missing login");
        if (register.get("password") == null) end(400, ResponseStatus.ERROR, "Missing password");
        if (register.get("email") == null) end(400, ResponseStatus.ERROR, "Missing email");
        if (register.get("displayName") == null) end(400, ResponseStatus.ERROR, "Missing display name");

        String login = register.get("login").getAsString();
        String password = register.get("password").getAsString();
        String email = register.get("email").getAsString();
        String displayName = register.get("displayName").getAsString();

        if (password == null || password.isEmpty() || password.isBlank()) {
            end(400, ResponseStatus.ERROR, "Missing Password");
        }
        assert password != null;

        if (!email.matches("^[a-zA-Z0-9.!#$%&’*+/=?^_`{|}~-]+@[a-zA-Z0-9-]+(?:\\.[a-zA-Z0-9-]+)*$")) {
            end(400, ResponseStatus.ERROR, "Invalid Email");
        }

        if (displayName == null || displayName.isEmpty() || displayName.isBlank()) {
            end(400, ResponseStatus.ERROR, "Missing Display Name");
        }
        assert displayName != null;

        if (getUserByLogin(req, res) != null) {
            end(400, ResponseStatus.ERROR, "A user with this login already exists.");
        }

        if (getUserByEmail(req, res) != null) {
            end(400, ResponseStatus.ERROR, "A user with this email already exists.");
        }

        UserEntity user = null;
        try {
            user = new UserEntity(login, password, email, displayName, Role.USER);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            e.printStackTrace();
            end(500, ResponseStatus.ERROR, "Internal Server Error");
        }
        assert user != null;

        try {
            user.create();
        } catch (SQLException e) {
            e.printStackTrace();
            end(500, ResponseStatus.ERROR, "Internal Server Error");
        }

        return gson.toJson(new BasicResponse(ResponseStatus.SUCCESS, "Created new user with ID " + user.getId(), gson.toJsonTree(user)));
    }

    private String loginUser(Request req, Response res) {
        JsonObject login = jsonFromBody(req);

        if (login.has("login") && login.has("email")) {
            end(400, ResponseStatus.ERROR, "Cannot have both login and email");
        }

        if (!login.has("login") && !login.has("email")) {
            end(400, ResponseStatus.ERROR, "Missing login or email");
        }

        if (login.has("login") && (login.get("login").getAsString().isEmpty() || login.get("login").getAsString().isBlank())) {
            end(400, ResponseStatus.ERROR, "Login is empty.");
        }

        if (login.has("email") && (login.get("email").getAsString().isEmpty() || login.get("email").getAsString().isBlank())) {
            end(400, ResponseStatus.ERROR, "Email is empty.");
        }

        if (!login.has("password") || login.get("password").getAsString().isEmpty() || login.get("password").getAsString().isBlank()) {
            end(400, ResponseStatus.ERROR, "Missing Password");
        }

        UserEntity user = null;
        try {
            if (login.has("login")) {
                user = UserEntity.getUserEntityByLogin(login.get("login").getAsString());
            } else if (login.has("email")) {
                user = UserEntity.getUserEntityByEmail(login.get("email").getAsString());
            }
        } catch (SQLException e) {
            e.printStackTrace();
            end(500, ResponseStatus.ERROR, "Internal Server Error");
        }

        if (user == null) end(400 ,ResponseStatus.ERROR, "User does not exist");
        assert user != null;

        if (!HashPassword.checkPassword(login.get("password").getAsString(), user.getSalt(), user.getHashedPassword())) {
            end(400, ResponseStatus.ERROR, "Incorrect Password");
        }

        res.header("Authorization", "Bearer " + user.getToken());
        if (user.getToken().isEmpty() || user.getToken().isBlank()) user.setToken(JWTManager.generateToken());
        return gson.toJson(new BasicResponse(ResponseStatus.SUCCESS, "Logged in successfully", gson.toJsonTree(user)));
    }

    private String getAllUsers(Request req, Response res) {
        before("", this.checkPermission(req, res, Role.ADMIN));

        if (req.queryParams("limit") == null) {
            try {
                return gson.toJson(new BasicResponse(ResponseStatus.SUCCESS, gson.toJsonTree(UserEntity.getAllUserEntities())));
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }

        Long limit = null;
        try {
            limit = req.queryParams("limit") == null ? null : Long.parseLong(req.queryParams("limit"));
        } catch (NumberFormatException e) {
            end(400, ResponseStatus.ERROR, "Limit must be a number.");
        }

        Long offset = null;
        try {
            offset = req.queryParams("offset") == null ? null : Long.parseLong(req.queryParams("offset"));
        } catch (NumberFormatException e) {
            end(400, ResponseStatus.ERROR, "Offset must be a number.");
        }

        try {
            if (limit != null) {
                if (offset != null) {
                    return gson.toJson(new BasicResponse(ResponseStatus.SUCCESS, gson.toJsonTree(UserEntity.getAllUserEntities(limit.intValue(), offset.intValue()))));
                } else {
                    return gson.toJson(new BasicResponse(ResponseStatus.SUCCESS, gson.toJsonTree(UserEntity.getAllUserEntities(limit))));
                }
            }
            return gson.toJson(new BasicResponse(ResponseStatus.SUCCESS, gson.toJsonTree(UserEntity.getAllUserEntities())));
        } catch (SQLException e) {
            e.printStackTrace();
            end(500, ResponseStatus.ERROR, "Internal Server Error");
        }

        return null;
    }

    private String getSelfUser(Request req, Response res) {
        return gson.toJson(new BasicResponse(ResponseStatus.SUCCESS, "User with id " + getUserByToken(req, res).getId(), gson.toJsonTree(getUserByToken(req, res))));
    }

    private String changeSelfPassword(Request req, Response res) {
        JsonObject changePassword = jsonFromBody(req);

        if (!changePassword.has("oldPassword") || changePassword.get("oldPassword").getAsString().isEmpty() || changePassword.get("oldPassword").getAsString().isBlank())
            end(400, ResponseStatus.ERROR, "Missing Old Password");


        if (!changePassword.has("newPassword") || changePassword.get("newPassword").getAsString().isEmpty() || changePassword.get("newPassword").getAsString().isBlank())
            end(400, ResponseStatus.ERROR, "Missing New Password");

        UserEntity user = getUserByToken(req, res);

        if (!HashPassword.checkPassword(changePassword.get("oldPassword").getAsString(), user.getSalt(), user.getHashedPassword()))
            end(400, ResponseStatus.ERROR, "Incorrect Password");


        String newPassword = changePassword.get("newPassword").getAsString();
        byte[][] hashed = HashPassword.generateSaltedHashedPassword(newPassword);
        user.setSalt(hashed[0]);
        user.setHashedPassword(hashed[1]);


        try {
            user.update();
        } catch (SQLException e) {
            e.printStackTrace();
            end(500, ResponseStatus.ERROR, "Internal Server Error");
        }

        return gson.toJson(new BasicResponse(ResponseStatus.SUCCESS, "Password Changed Successfully"));
    }

    private String updateUser(Request req, Response res) {
        UserEntity newUser = this.objectFromBody(req, UserEntity.class);

        UserEntity user;
        if (req.params(":id") != null) {
            try {
                user = UserEntity.getUserEntity(Long.parseLong(req.params(":id")));
            } catch (SQLException e) {
                e.printStackTrace();
                end(500, ResponseStatus.ERROR, "Internal Server Error");
            }
        }
        user = getUserByToken(req, res);

        if (newUser.getLogin() != null) user.setLogin(newUser.getLogin());
        if (newUser.getEmail() != null) user.setEmail(newUser.getEmail());
        if (newUser.getDisplayName() != null) user.setDisplayName(newUser.getDisplayName());
        if (newUser.getRole() != null && user.getRole() == Role.ADMIN) {
            this.checkPermission(req, res, Role.ADMIN);
            user.setRole(newUser.getRole());
        }
        if (newUser.getPermissions() != null) {
            this.checkPermission(req, res, Role.ADMIN);
            user.setPermissions(user.getPermissions());
        }

        if (!user.isValid()) end(400, ResponseStatus.ERROR, "Invalid user data.");

        try {
            user.update();
        } catch (SQLException e) {
            e.printStackTrace();
            end(500, ResponseStatus.ERROR, "Internal Server Error");
        }

        return gson.toJson(new BasicResponse(ResponseStatus.SUCCESS, "Updated User with ID " + user.getId(), gson.toJsonTree(user)));
    }

    private String invalidateTokens(Request req, Response res) {
        JsonObject tokens = jsonFromBody(req);
        List<String> tokensList = new ArrayList<>();
        if (tokens.has("tokens")) tokens.getAsJsonArray("tokens").forEach(jsonElement -> tokensList.add(jsonElement.getAsString()));
        if (tokens.has("token")) tokensList.add(tokens.get("token").getAsString());
        tokensList.add(getToken(req));
        for (String token : tokensList) {
            try {
                token = token.replace("Bearer ", "");
                UserEntity user = UserEntity.getUserEntityByToken(token);
                if (user == null) {
                    tokens.remove(token);
                    continue;
                }
                user.setToken("");
                user.update();
            } catch (SQLException e) {
                e.printStackTrace();
                end(500, ResponseStatus.ERROR, "Internal Server Error");
            }
        }
        res.header("Authorization", "");

        return gson.toJson(new BasicResponse(ResponseStatus.SUCCESS, "Invalidated token(s) " + gson.toJsonTree(tokensList)));
    }

    private String logoutSelf(Request req, Response res) {
        before(Authentication::authenticate);
        res.header("Authorization", "");
        return gson.toJson(new BasicResponse(ResponseStatus.SUCCESS, "Logged out successfully"));
    }
}
