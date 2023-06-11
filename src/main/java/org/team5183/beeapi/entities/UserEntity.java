package org.team5183.beeapi.entities;

import com.google.gson.*;
import com.google.gson.annotations.Expose;
import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.team5183.beeapi.authentication.HashPassword;
import org.team5183.beeapi.authentication.JWTManager;
import org.team5183.beeapi.constants.Permission;
import org.team5183.beeapi.constants.Role;
import org.team5183.beeapi.runnables.DatabaseRequestRunnable;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;

@DatabaseTable(tableName = "bee_users")
public class UserEntity {
    @Expose
    @DatabaseField(generatedId = true)
    private @NotNull Long id;

    @Expose
    @DatabaseField(canBeNull = false, unique = true)
    private @NotNull String login;

    @Expose
    @DatabaseField(canBeNull = false, unique = true)
    private @NotNull String email;

    @Expose
    @DatabaseField(canBeNull = false)
    private @NotNull String displayName;

    @DatabaseField(canBeNull = false, dataType = DataType.BYTE_ARRAY)
    private @NotNull byte[] hashedPassword;

    @DatabaseField(canBeNull = false, dataType = DataType.BYTE_ARRAY)
    private @NotNull byte[] salt;

    @DatabaseField(canBeNull = false)
    private @NotNull String token;

    @Expose
    @DatabaseField(canBeNull = false)
    private @NotNull Role role;

    @Expose
    @DatabaseField(canBeNull = false)
    private @NotNull String permissions;

    private transient Collection<Permission> permissionsList;

    /**
     * @param login The login (username) of the user, only used for logging in and as a unique identifier.
     * @param email The email of the user.
     * @param displayName The display name of the user, used for displaying the user's name.
     * @param password The password of the user, will be hashed and salted during construction.
     * @param role The role of the user, used for determining permissions.
     * @throws NoSuchAlgorithmException
     * @throws InvalidKeySpecException
     */
    public UserEntity(@NotNull String login, @NotNull String password, @NotNull String email, @NotNull String displayName, @NotNull Role role) throws NoSuchAlgorithmException, InvalidKeySpecException {
        this.login = login;
        this.email = email;
        this.displayName = displayName;
        this.token = JWTManager.generateToken();
        byte[][] saltedPassword = HashPassword.generateSaltedHashedPassword(password);
        this.salt = saltedPassword[0];
        this.hashedPassword = saltedPassword[1];
        this.role = role;
        this.permissionsList = new HashSet<>();
        this.permissions = new Gson().toJson(this.permissionsList);
    }

    /**
     * This constructor is and should only be used by JPA and ORMLite.
     */
    public UserEntity() {
//        if (this.permissions.isEmpty() || this.permissions.isBlank() || this.permissions.equals("[]") || this.permissions.equals("null")) {
//            this.permissionsList = new HashSet<>();
//            return;
//        }
//        try {
//            this.permissionsList = new Gson().fromJson(this.permissions, Collection.class);
//        } catch (JsonSyntaxException e) {
//            this.permissionsList = new HashSet<>();
//        }
    }

    /**
     * Gets the user with the specified ID.
     * @param id The ID of the user.
     * @return The user with the specified ID, or null if no user exists with that ID.
     * @throws SQLException If an error occurs while querying the database.
     */
    @Nullable
    public static UserEntity getUserEntity(long id) throws SQLException {
        CompletableFuture<UserEntity> future = DatabaseRequestRunnable.userQuery(DatabaseRequestRunnable.getUserDao().queryBuilder().where().eq("id", id).prepare());
        AtomicReference<UserEntity> userEntity = new AtomicReference<>();
        AtomicReference<Throwable> throwable = new AtomicReference<>();
        future.whenComplete((ue, t) -> {
            throwable.set(t);
            userEntity.set(ue);
        });

        if (throwable.get() != null) {
            throw new SQLException(throwable.get());
        }

        return userEntity.get();
    }

    /**
     * Gets the user with the specified login.
     * @param login The login (username) of the user.
     * @return The user with the specified login, or null if no user exists with that login.
     * @throws SQLException If an error occurs while querying the database.
     */
    @Nullable
    public static UserEntity getUserEntityByLogin(String login) throws SQLException {
        CompletableFuture<UserEntity> future = DatabaseRequestRunnable.userQuery(DatabaseRequestRunnable.getUserDao().queryBuilder().where().eq("login", login).prepare());
        AtomicReference<UserEntity> userEntity = new AtomicReference<>();
        AtomicReference<Throwable> throwable = new AtomicReference<>();
        future.whenComplete((ue, t) -> {
            throwable.set(t);
            userEntity.set(ue);
        });

        if (throwable.get() != null) {
            throw new SQLException(throwable.get());
        }

        return userEntity.get();
    }

    public static List<UserEntity> getAllUserEntities() throws SQLException {
        CompletableFuture<List<UserEntity>> future = DatabaseRequestRunnable.userQueryMultiple(DatabaseRequestRunnable.getUserDao().queryBuilder().prepare());
        AtomicReference<List<UserEntity>> userEntities = new AtomicReference<>();
        AtomicReference<Throwable> throwable = new AtomicReference<>();
        future.whenComplete((ue, t) -> {
            throwable.set(t);
            userEntities.set(ue);
        });

        if (throwable.get() != null) {
            throw new SQLException(throwable.get());
        }

        return userEntities.get();
    }

    /**
     * Gets the user with the specified email.
     * @param email The email of the user.
     * @return The user with the specified email, or null if no user exists with that email.
     * @throws SQLException If an error occurs while querying the database.
     */
    @Nullable
    public static UserEntity getUserEntityByEmail(String email) throws SQLException {
        CompletableFuture<UserEntity> future = DatabaseRequestRunnable.userQuery(DatabaseRequestRunnable.getUserDao().queryBuilder().where().eq("email", email).prepare());
        AtomicReference<UserEntity> userEntity = new AtomicReference<>();
        AtomicReference<Throwable> throwable = new AtomicReference<>();
        future.whenComplete((ue, t) -> {
            throwable.set(t);
            userEntity.set(ue);
        });

        if (throwable.get() != null) {
            throw new SQLException(throwable.get());
        }

        return userEntity.get();
    }

    /**
     * Gets the user with the specified token
     * @param @token The token of the user.
     * @return The user with the specified token, or null if no user exists with that token.
     * @throws SQLException If an error occurs while querying the database.
     */
    @Nullable
    public static UserEntity getUserEntityByToken(String token) throws SQLException {
        CompletableFuture<UserEntity> future = DatabaseRequestRunnable.userQuery(DatabaseRequestRunnable.getUserDao().queryBuilder().where().eq("token", token).prepare());
        AtomicReference<UserEntity> userEntity = new AtomicReference<>();
        AtomicReference<Throwable> throwable = new AtomicReference<>();
        future.whenComplete((ue, t) -> {
            throwable.set(t);
            userEntity.set(ue);
        });

        if (throwable.get() != null) {
            throw new SQLException(throwable.get());
        }

        return userEntity.get();
    }

    /**
     * Creates the user in the database.
     * TODO: make this not like this, allow it to actually run through the database request runnable's cache instead of just forcing synchronization bc
     * @throws SQLException If an error occurs while creating the user in the database.
     */
    public synchronized void create() throws SQLException {
        DatabaseRequestRunnable.getUserDao().createOrUpdate(this);
    }

    /**
     * Updates the user in the database.
     * @throws SQLException If an error occurs while updating the user in the database.
     */
    public void update() throws SQLException {
        DatabaseRequestRunnable.userStatement(DatabaseRequestRunnable.getUserDao().updateBuilder().where().eq("id", this.id).prepare());
    }

    /**
     * Deletes the user from the database.
     * @throws SQLException If an error occurs while deleting the user from the database.
     */
    public void delete() throws SQLException {
        DatabaseRequestRunnable.userStatement(DatabaseRequestRunnable.getUserDao().deleteBuilder().where().eq("id", this.id).prepare());
    }

    /**
     * @return The ID of the user.
     */
    public @NotNull Long getId() {
        return id;
    }

    /**
     * @return The login (username) of the user.
     */
    public @NotNull String getLogin() {
        return login;
    }

    /**
     * @param login The login (username) of the user.
     */
    public void setLogin(@NotNull String login) {
        this.login = login;
    }

    /**
     * @return The email of the user.
     */
    public @NotNull String getEmail() {
        return email;
    }

    /**
     * @param email The email of the user.
     */
    public void setEmail(@NotNull String email) {
        this.email = email;
    }

    /**
     * @return The display name of the user.
     */
    public @NotNull String getDisplayName() {
        return displayName;
    }

    /**
     * @param displayName The display name of the user.
     */
    public void setDisplayName(@NotNull String displayName) {
        this.displayName = displayName;
    }

    /**
     * @return The token of the user.
     */
    public @NotNull String getToken() {
        return token;
    }

    /**
     * @param token The token of the user.
     */
    public void setToken(@NotNull String token) {
        this.token = token;
    }

    /**
     * @return The hashed password of the user.
     */
    public @NotNull byte[] getHashedPassword() {
        return hashedPassword;
    }

    /**
     * @param hashedPassword The hashed password of the user.
     */
    public void setHashedPassword(@NotNull byte[] hashedPassword) {
        this.hashedPassword = hashedPassword;
    }

    /**
     * @return The salt of the user.
     */
    public @NotNull byte[] getSalt() {
        return salt;
    }

    /**
     * @param salt The salt of the user.
     */
    public void setSalt(byte[] salt) {
        this.salt = salt;
    }

    /**
     * @return The role of the user.
     */
    public @NotNull Role getRole() {
        return role;
    }

    /**
     * @param role The role of the user.
     */
    public void setRole(@NotNull Role role) {
        this.role = role;
    }

    /**
     * @return The permissions of the user.
     */
    public @NotNull String getPermissions() {
        return permissions;
    }

    /**
     * @param permissions The permissions of the user.
     */
    public void setPermissions(@NotNull String permissions) {
        this.permissions = permissions;
    }

    /**
     * @return The permissions of the user.
     */
    public Collection<Permission> getPermissionsList() {
        if (this.permissionsList == null) {
            try {
                this.permissionsList = new HashSet<>();
                JsonArray obj = new Gson().fromJson(this.permissions, JsonArray.class);
                obj.forEach((element) -> {
                    this.permissionsList.add(Permission.valueOf(element.getAsString()));
                });
            } catch (JsonSyntaxException e) {
                this.permissionsList = new HashSet<>();
            }
        }
        return permissionsList;
    }

    /**
     * @param permission The permission to add to the user.
     */
    public void addPermission(Permission permission) {
        this.permissionsList.add(permission);
        this.permissions = new Gson().toJson(this.permissionsList);
    }

    /**
     * @param permissionsList The permissions to add to the user.
     */
    public void setPermissionsList(Collection<Permission> permissionsList) {
        this.permissionsList = permissionsList;
        this.permissions = new Gson().toJson(this.permissionsList);
    }
}
