package org.team5183.beeapi.entities;

import com.google.gson.*;
import com.google.gson.annotations.Expose;
import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import org.jetbrains.annotations.NotNull;
import org.team5183.beeapi.authentication.HashPassword;
import org.team5183.beeapi.authentication.JWTManager;
import org.team5183.beeapi.constants.Permission;
import org.team5183.beeapi.constants.Role;

import javax.persistence.*;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

@Entity
@Table(name = "bee_users")
public class UserEntity {
    @SuppressWarnings("NotNullFieldNotInitialized")
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Expose
    private @NotNull Long id;

    @Expose
    @Column(nullable = false)
    private @NotNull String login;

    @Expose
    @Column(nullable = false)
    private @NotNull String email;

    @Expose
    @Column(nullable = false)
    private @NotNull String displayName;

    @Column(nullable = false)
    @DatabaseField(canBeNull = false, dataType = DataType.BYTE_ARRAY)
    private byte[] hashedPassword;

    @Column(nullable = false)
    @DatabaseField(canBeNull = false, dataType = DataType.BYTE_ARRAY)
    private byte[] salt;

    @Column(nullable = false)
    private @NotNull String token;

    @Expose
    @Column(nullable = false)
    private @NotNull Role role;

    @Expose
    @Column(nullable = false)
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
    public UserEntity(@NotNull String login, @NotNull String password, @NotNull String token,  @NotNull String email, @NotNull String displayName, @NotNull Role role) throws NoSuchAlgorithmException, InvalidKeySpecException {
        this.login = login;
        this.email = email;
        this.displayName = displayName;
        this.token = token;
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
