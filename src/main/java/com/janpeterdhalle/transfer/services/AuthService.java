package com.janpeterdhalle.transfer.services;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.janpeterdhalle.transfer.models.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
@Slf4j
public class AuthService {
    @Value("${spring.app.jwtSecret}")
    private String secret;

    public String generateToken(User user) {
        log.info("Generating JWT for email user: {}", user.getEmail());
        String token = JWT.create()
                          .withSubject("User Details")
                          .withClaim("email", user.getEmail())
                          .withClaim("roles", user.getAuthorities().stream().map(Object::toString).toList().get(0))
                          .withIssuedAt(new Date())
                          .withIssuer("jp_file_share")
                          .sign(Algorithm.HMAC256(secret));
        System.out.println("GENERATED TOKEN: " + token);
        return token;
    }

    public String validateTokenAndRetrieveSubject(String token) throws JWTVerificationException {
        JWTVerifier verifier = JWT.require(Algorithm.HMAC256(secret))
                                  .withSubject("User Details")
                                  .withIssuer("jp_file_share")
                                  .build();

        DecodedJWT jwt = verifier.verify(token);
        return jwt.getClaim("email").asString();
    }
}
