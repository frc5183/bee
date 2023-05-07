package org.team5183.beeapi.authentication;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;

public class HashPassword {
    private static final Logger logger = LogManager.getLogger(HashPassword.class);


    /**
     * Salts & Hashes a password using PBKDF2 with a random salt.
     *
     * @param password The password to hash
     * @return A string array where the first element is the salt and the second element is the hashed password
     * @throws NoSuchAlgorithmException
     * @throws InvalidKeySpecException
     */
    public static String[] generateSaltedHashedPassword(String password) throws NoSuchAlgorithmException, InvalidKeySpecException {
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[32];
        random.nextBytes(salt);

        byte[] hash = generateHashedPassword(password, salt);

        return new String[]{salt.toString(), hash.toString()};
    }

    /**
     * Checks if a password matches a salted hash
     *
     * @param password       The password to check
     * @param salt           The salt used to hash the password
     * @param hashedPassword The hashed password
     * @return True if the password matches the hash, false otherwise
     * @throws NoSuchAlgorithmException
     * @throws InvalidKeySpecException
     */
    public static boolean checkPassword(String password, String salt, String hashedPassword) throws NoSuchAlgorithmException, InvalidKeySpecException {
        byte[] saltBytes = salt.getBytes();
        byte[] hashedPasswordBytes = hashedPassword.getBytes();
        byte[] generatedHash = generateHashedPassword(password, saltBytes);
        logger.info("password: " + password);
        logger.info("salt: " + salt);
        logger.info("Generated hash: " + generatedHash.toString());
        return MessageDigest.isEqual(generatedHash, hashedPasswordBytes);
    }

    private static byte[] generateHashedPassword(String password, byte[] salt) throws NoSuchAlgorithmException, InvalidKeySpecException {
        KeySpec spec = new PBEKeySpec(password.toCharArray(), salt, 65536, 128);

        byte[] hash;
        try {
            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
            hash = factory.generateSecret(spec).getEncoded();
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            logger.fatal("Failed to generate salted hash", e);
            throw e;
        }
        return hash;
    }
}
