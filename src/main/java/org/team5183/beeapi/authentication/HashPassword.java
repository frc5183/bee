package org.team5183.beeapi.authentication;

import at.favre.lib.crypto.bcrypt.BCrypt;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.util.ArrayUtils;

import java.security.MessageDigest;

public class HashPassword {
    private static final Logger logger = LogManager.getLogger(HashPassword.class);


    /**
     * Salts & Hashes a password using PBKDF2 with a random salt.
     *
     * @param password The password to hash
     * @return A string array where the first element is the salt and the second element is the hashed password
     */
    public static byte[][] generateSaltedHashedPassword(String password) {
        byte[] salt = new byte[16];

        for (int i = 0; i < 16; i++) {
            salt[i] = (byte) (Math.random() * 256);
        }

        byte[] hash = generateHashedPassword(password, salt);

        return new byte[][]{salt, hash};
    }

    /**
     * Checks if a password matches a salted hash
     *
     * @param password       The password to check
     * @param salt           The salt used to hash the password
     * @param hashedPassword The hashed password
     * @return True if the password matches the hash, false otherwise
     */
    public static boolean checkPassword(String password, byte[] salt, byte[] hashedPassword) {
        byte[] saltBytes = salt;
        byte[] hashedPasswordBytes = hashedPassword;
        byte[] generatedHash = generateHashedPassword(password, saltBytes);
        return MessageDigest.isEqual(generatedHash, hashedPasswordBytes);
    }

    private static byte[] generateHashedPassword(String password, byte[] salt) {
        return BCrypt.withDefaults().hash(6, salt, password.getBytes());
    }
}
