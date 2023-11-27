package org.team5183.beeapi.entities;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonSyntaxException;
import com.google.gson.annotations.Expose;
import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import org.jetbrains.annotations.Nullable;
import org.team5183.beeapi.authentication.HashPassword;
import org.team5183.beeapi.authentication.JWTManager;
import org.team5183.beeapi.constants.Permission;
import org.team5183.beeapi.constants.Role;
import org.team5183.beeapi.runnables.DatabaseRunnable;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;

@DatabaseTable(tableName = "bee_users")
public class UserEntity {
    @Expose(serialize = true, deserialize = false)
    @DatabaseField(generatedId = true)
    private Long id;

    @Expose
    @DatabaseField(canBeNull = false, unique = true)
    private String login;

    @Expose
    @DatabaseField(canBeNull = false, unique = true)
    private String email;

    @Expose
    @DatabaseField(canBeNull = false)
    private String displayName;

    @DatabaseField(canBeNull = false, dataType = DataType.BYTE_ARRAY)
    private byte[] hashedPassword;

    @DatabaseField(canBeNull = false, dataType = DataType.BYTE_ARRAY)
    private byte[] salt;

    @Expose(serialize = true, deserialize = false)
    @DatabaseField(canBeNull = true)
    private String token;

    @Expose
    @DatabaseField(canBeNull = false)
    private Role role;

    @Expose
    @DatabaseField(canBeNull = false)
    private String permissions;

    private transient Collection<Permission> permissionsList;

    /**
     * @param login The login (username) of the user, only used for logging in and as a unique identifier.
     * @param email The email of the user.
     * @param displayName The display name of the user, used for displaying the user's name.
     * @param password The password of the user, will be hashed and salted during construction.
     * @param role The role of the user, used for determining permissions.
     * @throws NoSuchAlgorithmException If the hashing algorithm is not found.
     * @throws InvalidKeySpecException If the key spec is invalid.
     */
    public UserEntity(String login, String password, String email, String displayName, Role role) throws NoSuchAlgorithmException, InvalidKeySpecException {
        this.login = login;
        this.email = email;
        this.displayName = displayName;
        byte[][] saltedPassword = HashPassword.generateSaltedHashedPassword(password);
        this.salt = saltedPassword[0];
        this.hashedPassword = saltedPassword[1];
        this.role = role;
        this.permissionsList = new HashSet<>();
        this.permissions = new Gson().toJson(this.permissionsList);
    }

    /**
     *  Default constructor for ORMLite.
     */
    public UserEntity() {

    }

    /**
     * Gets the user with the specified ID.
     * @param id The ID of the user.
     * @return The user with the specified ID, or null if no user exists with that ID.
     * @throws SQLException If an error occurs while querying the database.
     */
    @Nullable
    public static UserEntity getUserEntity(long id) throws SQLException {
        CompletableFuture<Optional<List<UserEntity>>> future = DatabaseRunnable.userRequest(new DatabaseRunnable.DatabaseRequest<>(DatabaseRunnable.getUserDao().queryBuilder().where().eq("id", id).prepare()));
        AtomicReference<Optional<List<UserEntity>>> entities = new AtomicReference<>();
        AtomicReference<Throwable> throwable = new AtomicReference<>();
        future.whenComplete((ie, t) -> {
            throwable.set(t);
            entities.set(ie);
        });
        future.join();
        if (throwable.get() != null) throw (SQLException) throwable.get();
        if (entities.get() == null) throw new NullPointerException("Entities is null");
        if (entities.get().isEmpty()) return null;
        if (entities.get().get().size() > 1 || entities.get().get().size() < 1) return null;
        return entities.get().get().get(0);
    }

    /**
     * Gets the user with the specified login.
     * @param login The login (username) of the user.
     * @return The user with the specified login, or null if no user exists with that login.
     * @throws SQLException If an error occurs while querying the database.
     */
    @Nullable
    public static UserEntity getUserEntityByLogin(String login) throws SQLException {
        CompletableFuture<Optional<List<UserEntity>>> future = DatabaseRunnable.userRequest(new DatabaseRunnable.DatabaseRequest<>(DatabaseRunnable.getUserDao().queryBuilder().where().eq("login", login).prepare()));
        AtomicReference<Optional<List<UserEntity>>> entities = new AtomicReference<>();
        AtomicReference<Throwable> throwable = new AtomicReference<>();
        future.join();
        future.whenComplete((ie, t) -> {
            throwable.set(t);
            entities.set(ie);
        });
        if (throwable.get() != null) throw (SQLException) throwable.get();
        if (entities.get() == null) throw new NullPointerException("Entities is null");
        if (entities.get().isEmpty()) return null;
        if (entities.get().get().size() > 1 || entities.get().get().size() < 1) return null;
        return entities.get().get().get(0);
    }

    /**
     * Gets the user with the specified email.
     * @param email The email of the user.
     * @return The user with the specified email, or null if no user exists with that email.
     * @throws SQLException If an error occurs while querying the database.
     */
    @Nullable
    public static UserEntity getUserEntityByEmail(String email) throws SQLException {
        CompletableFuture<Optional<List<UserEntity>>> future = DatabaseRunnable.userRequest(new DatabaseRunnable.DatabaseRequest<>(DatabaseRunnable.getUserDao().queryBuilder().where().eq("email", email).prepare()));
        AtomicReference<Optional<List<UserEntity>>> entities = new AtomicReference<>();
        AtomicReference<Throwable> throwable = new AtomicReference<>();
        future.join();
        future.whenComplete((ie, t) -> {
            throwable.set(t);
            entities.set(ie);
        });
        if (throwable.get() != null) throw (SQLException) throwable.get();
        if (entities.get() == null) throw new NullPointerException("Entities is null");
        if (entities.get().isEmpty()) return null;
        if (entities.get().get().size() > 1 || entities.get().get().size() < 1) return null;
        return entities.get().get().get(0);
    }

    /**
     * Gets the user with the specified token
     * @param token The token of the user.
     * @return The user with the specified token, or null if no user exists with that token.
     * @throws SQLException If an error occurs while querying the database.
     */
    @Nullable
    public static UserEntity getUserEntityByToken(String token) throws SQLException {
        CompletableFuture<Optional<List<UserEntity>>> future = DatabaseRunnable.userRequest(new DatabaseRunnable.DatabaseRequest<>(DatabaseRunnable.getUserDao().queryBuilder().where().eq("token", token).prepare()));
        AtomicReference<Optional<List<UserEntity>>> entities = new AtomicReference<>();
        AtomicReference<Throwable> throwable = new AtomicReference<>();
        future.join();
        future.whenComplete((ie, t) -> {
            throwable.set(t);
            entities.set(ie);
        });
        if (throwable.get() != null) throw (SQLException) throwable.get();
        if (entities.get() == null) throw new NullPointerException("Entities is null");
        if (entities.get().isEmpty()) return null;
        if (entities.get().get().size() > 1 || entities.get().get().size() < 1) return null;

        DecodedJWT jwt = JWTManager.decodeToken(token);
        if (!entities.get().get().get(0).getId().equals(jwt.getClaim("id").asLong())) return null;
        if (!entities.get().get().get(0).getLogin().equals(jwt.getClaim("login").asString())) return null;
        return entities.get().get().get(0);
    }

    /**
     * Gets a list of all users in the database.
     * @return A list of all items in the database.
     * @throws SQLException If an error occurs while querying the database.
     * @see #getAllUserEntities(long)
     * @see #getAllUserEntities(int, int)
     */
    public static List<UserEntity> getAllUserEntities() throws SQLException {
        CompletableFuture<Optional<List<UserEntity>>> future = DatabaseRunnable.userRequest(new DatabaseRunnable.DatabaseRequest<>(DatabaseRunnable.getUserDao().queryBuilder().prepare()));
        AtomicReference<Optional<List<UserEntity>>> entities = new AtomicReference<>();
        AtomicReference<Throwable> throwable = new AtomicReference<>();
        future.join();
        future.whenComplete((ie, t) -> {
            throwable.set(t);
            entities.set(ie);
        });
        if (throwable.get() != null) throw (SQLException) throwable.get();
        if (entities.get() == null) throw new NullPointerException("Entities is null");
        if (entities.get().isEmpty()) return null;
        if (entities.get().get().size() < 1) return null;
        return entities.get().get();
    }

    /**
     * Gets a list of all users in the database.
     * @param limit The maximum number of items to return.
     * @return A list of all users in the database.
     * @throws SQLException If an error occurs while querying the database.
     * @see #getAllUserEntities()
     * @see #getAllUserEntities(int, int)
     */
    public static List<ItemEntity> getAllUserEntities(long limit) throws SQLException {
        CompletableFuture<Optional<List<ItemEntity>>> future = DatabaseRunnable.itemRequest(new DatabaseRunnable.DatabaseRequest<>(DatabaseRunnable.getItemDao().queryBuilder().limit(limit).prepare()));
        AtomicReference<Optional<List<ItemEntity>>> entities = new AtomicReference<>();
        AtomicReference<Throwable> throwable = new AtomicReference<>();
        future.join();
        future.whenComplete((ie, t) -> {
            throwable.set(t);
            entities.set(ie);
        });
        if (throwable.get() != null) throw (SQLException) throwable.get();
        if (entities.get() == null) throw new NullPointerException("Entities is null");
        if (entities.get().isEmpty()) return null;
        if (entities.get().get().size() < 1) return null;
        return entities.get().get();
    }

    /**
     * Gets a list of all users in the database.
     * @param limit The maximum number of users to return.
     * @param offset The offset to start at.
     * @return A list of all users in the database.
     * @throws SQLException If an error occurs while querying the database.
     * @see #getAllUserEntities()
     * @see #getAllUserEntities(long)
     */
    public static List<UserEntity> getAllUserEntities(int limit, int offset) throws SQLException {
        CompletableFuture<Optional<List<UserEntity>>> future = DatabaseRunnable.userRequest(new DatabaseRunnable.DatabaseRequest<>(DatabaseRunnable.getUserDao().queryBuilder().prepare()));
        AtomicReference<Optional<List<UserEntity>>> entities = new AtomicReference<>();
        AtomicReference<Throwable> throwable = new AtomicReference<>();
        future.join();
        future.whenComplete((ie, t) -> {
            throwable.set(t);
            entities.set(ie);
        });
        if (throwable.get() != null) throw (SQLException) throwable.get();
        if (entities.get() == null) throw new NullPointerException("Entities is null");
        if (entities.get().isEmpty()) return null;
        if (entities.get().get().size() < 1) return null;
        List<UserEntity> userEntities = entities.get().get().subList(offset, entities.get().get().size());
        if (userEntities.size() > limit) userEntities = userEntities.subList(0, limit);
        return userEntities;
    }

    /**
     * Creates the item in the database.
     * @throws SQLException If an error occurs while creating the user in the database.
     */
    public synchronized void create() throws SQLException {
        CompletableFuture<Optional<List<UserEntity>>> future = DatabaseRunnable.userRequest(new DatabaseRunnable.DatabaseRequest<>(this, DatabaseRunnable.DatabaseRequest.RequestType.INSERT));
        AtomicReference<Throwable> throwable = new AtomicReference<>();

        future.join();
        future.whenComplete((ie, t) -> throwable.set(t));

        if (throwable.get() != null) throw (SQLException) throwable.get();
    }

    /**
     * Updates the item in the database.
     * @throws SQLException If an error occurs while updating the user in the database.
     */
    public void update() throws SQLException {
        CompletableFuture<Optional<List<UserEntity>>> future = DatabaseRunnable.userRequest(new DatabaseRunnable.DatabaseRequest<>(this, DatabaseRunnable.DatabaseRequest.RequestType.UPDATE));
        AtomicReference<Throwable> throwable = new AtomicReference<>();

        future.join();
        future.whenComplete((ie, t) -> throwable.set(t));

        if (throwable.get() != null) throw (SQLException) throwable.get();
    }

    /**
     * Deletes the item from the database.
     * @throws SQLException If an error occurs while deleting the user from the database.
     */
    public void delete() throws SQLException {
        CompletableFuture<Optional<List<UserEntity>>> future = DatabaseRunnable.userRequest(new DatabaseRunnable.DatabaseRequest<>(DatabaseRunnable.getUserDao().deleteBuilder().where().eq("id", this.id).prepare()));
        AtomicReference<Throwable> throwable = new AtomicReference<>();

        future.join();
        future.whenComplete((ie, t) -> throwable.set(t));

        if (throwable.get() != null) throw (SQLException) throwable.get();
    }

    /**
     * @return The ID of the user.
     */
    public Long getId() {
        return id;
    }

    /**
     * @return The login (username) of the user.
     */
    public String getLogin() {
        return login;
    }

    /**
     * @param login The login (username) of the user.
     */
    public void setLogin(String login) {
        this.login = login;
    }

    /**
     * @return The email of the user.
     */
    public String getEmail() {
        return email;
    }

    /**
     * @param email The email of the user.
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * @return The display name of the user.
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * @param displayName The display name of the user.
     */
    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    /**
     * @return The token of the user.
     */
    public String getToken() {
        return token;
    }

    /**
     * @param token The token of the user.
     */
    public void setToken(String token) {
        this.token = token;
    }

    /**
     * @return The hashed password of the user.
     */
    public byte[] getHashedPassword() {
        return hashedPassword;
    }

    /**
     * @param hashedPassword The hashed password of the user.
     */
    public void setHashedPassword(byte[] hashedPassword) {
        this.hashedPassword = hashedPassword;
    }

    /**
     * @return The salt of the user.
     */
    public byte[] getSalt() {
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
    public Role getRole() {
        return role;
    }

    /**
     * @param role The role of the user.
     */
    public void setRole(Role role) {
        this.role = role;
    }

    /**
     * @return The permissions of the user.
     */
    public String getPermissions() {
        return permissions;
    }

    /**
     * @param permissions The permissions of the user.
     */
    public void setPermissions(String permissions) {
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

    /**
     * Checks if the UserEntity is valid.
     * @return Whether the UserEntity is valid
     */
    public boolean isValid() {
        return this.login != null && !this.login.isEmpty() &&
                this.email != null && this.email.isEmpty() && this.email.matches("^[a-zA-Z0-9.!#$%&’*+/=?^_`{|}~-]+@[a-zA-Z0-9-]+(?:\\.[a-zA-Z0-9-]+)*$") &&
                this.displayName != null && !this.displayName.isEmpty() &&
                this.role != null;
    }
}
