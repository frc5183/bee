package org.team5183.beeapi.authentication;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.AlgorithmMismatchException;
import com.auth0.jwt.exceptions.IncorrectClaimException;
import com.auth0.jwt.exceptions.SignatureVerificationException;
import com.auth0.jwt.exceptions.TokenExpiredException;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.team5183.beeapi.ConfigurationParser;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

public class JWTManager {
    /**
     * Generates a signed JWT token.
     * @return A signed JWT token.
     * @param id The ID of the user.
     * @param login The login of the user.
     */
    public static String generateToken(Long id, String login) {
        Algorithm algorithm = Algorithm.HMAC384(ConfigurationParser.getConfiguration().jwtSecret);
        return JWT.create().withClaim("id", id).withClaim("login", login).withExpiresAt(Instant.now().plus(30, ChronoUnit.DAYS)).sign(algorithm);
    }


    /**
     * @param signedToken The signed JWT token to decode.
     * @return The decoded JWT token.
     * @throws AlgorithmMismatchException If the algorithm used to sign the token is not the same as the one used to decode it.
     * @throws SignatureVerificationException If the signature of the token is invalid.
     * @throws TokenExpiredException If the token has expired.
     * @throws IncorrectClaimException If the token contains an incorrect claim.
     */
    public static DecodedJWT decodeToken(String signedToken) throws AlgorithmMismatchException, SignatureVerificationException, TokenExpiredException, IncorrectClaimException {
        Algorithm algorithm = Algorithm.HMAC384(ConfigurationParser.getConfiguration().jwtSecret);
        return JWT.require(algorithm).build().verify(signedToken);
    }
}
