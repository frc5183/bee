package org.team5183.beeapi.entities;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonSyntaxException;
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
    private @NotNull Long id;

    @Column(nullable = false)
    private @NotNull String login;

    @Column(nullable = false)
    private @NotNull String email;

    @Column(nullable = false)
    private @NotNull String displayName;

    @Column(nullable = false)
    private @NotNull String hashedPassword;

    @Column(nullable = false)
    private @NotNull String salt;

    @Column(nullable = false)
    private @NotNull String token;

    @Column(nullable = false)
    private @NotNull Role role;

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
        String[] saltedPassword = HashPassword.generateSaltedHashedPassword(password);
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

    public @NotNull Long getId() {
        return id;
    }

    public @NotNull String getLogin() {
        return login;
    }

    public void setLogin(@NotNull String login) {
        this.login = login;
    }

    public @NotNull String getEmail() {
        return email;
    }

    public void setEmail(@NotNull String email) {
        this.email = email;
    }

    public @NotNull String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(@NotNull String displayName) {
        this.displayName = displayName;
    }

    public @NotNull String getToken() {
        return token;
    }

    public void setToken(@NotNull String token) {
        this.token = token;
    }

    public @NotNull String getHashedPassword() {
        return hashedPassword;
    }

    public void setHashedPassword(@NotNull String hashedPassword) {
        this.hashedPassword = hashedPassword;
    }

    public @NotNull String getSalt() {
        return salt;
    }

    public void setSalt(@NotNull String salt) {
        this.salt = salt;
    }

    public @NotNull Role getRole() {
        return role;
    }

    public void setRole(@NotNull Role role) {
        this.role = role;
    }

    public @NotNull String getPermissions() {
        return permissions;
    }

    public void setPermissions(@NotNull String permissions) {
        this.permissions = permissions;
    }

    public Collection<Permission> getPermissionsList() {
        if (this.permissionsList == null) {
            try {
                this.permissionsList = new Gson().fromJson(this.permissions, Collection.class);
            } catch (JsonSyntaxException e) {
                this.permissionsList = new HashSet<>();
            }
        }
        return permissionsList;
    }

    public void addPermission(Permission permission) {
        this.permissionsList.add(permission);
        this.permissions = new Gson().toJson(this.permissionsList);
    }

    public void setPermissionsList(Collection<Permission> permissionsList) {
        this.permissionsList = permissionsList;
        this.permissions = new Gson().toJson(this.permissionsList);
    }

    public JsonElement toJson() {
        JsonElement json = new Gson().toJsonTree(this);
        json.getAsJsonObject().remove("hashedPassword");
        json.getAsJsonObject().remove("salt");

        return json;
    }
}
