package org.team5183.beeapi.endpoints;

import com.google.gson.JsonObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.team5183.beeapi.authentication.HashPassword;
import org.team5183.beeapi.authentication.JWTManager;
import org.team5183.beeapi.constants.Permission;
import org.team5183.beeapi.constants.Role;
import org.team5183.beeapi.entities.ItemEntity;
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
            put("/register", this::registerUser);

            post("/login", this::loginUser);

            get("/all", this::getAllUsers);
            get("/all?limit=:limit", this::getAllUsers);

            path("/:id", () -> {
//                before(Authentication::authenticate);
//                before("", (req,res) -> Authentication.checkPermission(req, res, Role.ADMIN));

                get("", this::getSelfUser);

                patch("/overwritePassword", this::changeSelfPassword);

                patch("/update", this::updateSelf);
            });

            path("/me", () -> {
                before(Authentication::authenticate);

                get("", this::getSelfUser);

                patch("/changePassword", this::changeSelfPassword);

                patch("/update", this::updateSelf);

//                delete("", this::deleteSelf);
            });

            patch("/invalidateTokens", this::invalidateTokens);

            put("/logout", this::logoutSelf);
        });
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

        List<UserEntity> users = null;
        try {
            users = UserEntity.getAllUserEntities();
        } catch (SQLException e) {
            e.printStackTrace();
            end(500, ResponseStatus.ERROR, "Internal Server Error");
        }
        if (users == null) end(200, ResponseStatus.SUCCESS, gson.toJsonTree(users));
        assert users != null;

        if (req.params(":limit") != null && !req.params(":limit").isEmpty()) {
            List<UserEntity> usersCopy = new ArrayList<>(Integer.parseInt(req.params(":limit")));
            for (int i = 0; i < users.size(); i++) {
                usersCopy.add(users.get(i));
                if (i >= Integer.parseInt(req.params(":limit"))) {
                    break;
                }
            }
            return gson.toJson(new BasicResponse(ResponseStatus.SUCCESS, gson.toJsonTree(usersCopy)));
        }
        return gson.toJson(new BasicResponse(ResponseStatus.SUCCESS, gson.toJsonTree(users)));
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

    private String updateSelf(Request req, Response res) {
        JsonObject update = jsonFromBody(req);

        boolean loginExists = update.has("login") && !(update.get("login").getAsString().isEmpty() || update.get("login").getAsString().isBlank());
        boolean emailExists = update.has("email") && !(update.get("email").getAsString().isEmpty() || update.get("email").getAsString().isBlank());
        boolean displayNameExists = update.has("displayName") && !(update.get("displayName").getAsString().isEmpty() || update.get("displayName").getAsString().isBlank());

        if (!loginExists && !emailExists && !displayNameExists) end(400, ResponseStatus.ERROR, "Missing one or more: login, email, displayName");

        if (!update.get("email").getAsString().matches("^[a-zA-Z0-9.!#$%&’*+/=?^_`{|}~-]+@[a-zA-Z0-9-]+(?:\\.[a-zA-Z0-9-]+)*$")) end(400, ResponseStatus.ERROR, "Invalid Email");

        UserEntity user = getUserByToken(req, res);

        if (loginExists) {
            if (getUserByLogin(req, res) != null) end(400, ResponseStatus.ERROR, "Login already taken");

            user.setLogin(update.get("login").getAsString());
        }

        if (emailExists) {
            if (getUserByEmail(req, res) != null) end(400, ResponseStatus.ERROR, "Email already taken");

            user.setEmail(update.get("email").getAsString());
        }

        if (displayNameExists) user.setDisplayName(update.get("displayName").getAsString());

        // admin stuff
        boolean roleExists = update.has("role") && !(update.get("role").getAsString().isEmpty() || update.get("role").getAsString().isBlank());
        boolean permissionsExists = update.has("permissions") && !(update.get("permissions").getAsString().isEmpty() || update.get("permissions").getAsString().isBlank());

        if (roleExists || permissionsExists) {
            this.checkPermission(req, res, Role.ADMIN);
        }

        if (roleExists) {
            try {
                user.setRole(Role.valueOf(update.get("role").getAsString()));
            } catch (IllegalArgumentException e) {
                end(400, ResponseStatus.ERROR, "Invalid Role");
            }
        }

        if (permissionsExists) {
            try {
                update.get("permissions").getAsJsonArray().forEach(jsonElement -> user.addPermission(Permission.valueOf(jsonElement.getAsString())));
            } catch (IllegalArgumentException | IllegalStateException e) {
                end(400, ResponseStatus.ERROR, "Invalid Permissions");
            }
        }

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
                if (UserEntity.getUserEntityByToken(token) == null) {
                    tokens.remove(token);
                } else {
                    UserEntity user = UserEntity.getUserEntityByToken(token);
                    user.setToken("");
                    user.update();
                }
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
