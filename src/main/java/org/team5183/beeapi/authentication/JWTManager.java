package org.team5183.beeapi.authentication;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.*;
import com.auth0.jwt.interfaces.DecodedJWT;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

public class JWTManager {
    public static String generateToken() {
        Algorithm algorithm = Algorithm.HMAC384(System.getenv("JWT_SECRET"));
        return JWT.create().withExpiresAt(Instant.now().plus(30, ChronoUnit.DAYS)).sign(algorithm);
    }

    public static DecodedJWT decodeToken(String signedToken) throws AlgorithmMismatchException, SignatureVerificationException, TokenExpiredException, IncorrectClaimException {
        Algorithm algorithm = Algorithm.HMAC384(System.getenv("JWT_SECRET"));
        return JWT.require(algorithm).build().verify(signedToken);
    }
}
