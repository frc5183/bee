package org.team5183.beeapi.authentication;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.*;
import com.auth0.jwt.interfaces.DecodedJWT;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

public class JWTManager {
    /**
     * Generates a signed JWT token.
     * @return A signed JWT token.
     */
    public static String generateToken() {
        Algorithm algorithm = Algorithm.HMAC384(System.getenv("JWT_SECRET"));
        return JWT.create().withExpiresAt(Instant.now().plus(30, ChronoUnit.DAYS)).sign(algorithm);
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
        Algorithm algorithm = Algorithm.HMAC384(System.getenv("JWT_SECRET"));
        return JWT.require(algorithm).build().verify(signedToken);
    }
}
