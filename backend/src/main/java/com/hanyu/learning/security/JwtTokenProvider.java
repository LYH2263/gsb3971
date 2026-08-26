package com.hanyu.learning.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Arrays;
import java.util.Date;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class JwtTokenProvider {

    private final SecretKey key;
    private final long expireSeconds;

    public JwtTokenProvider(
        @Value("${elderly.jwt.secret}") String secret,
        @Value("${elderly.jwt.expire-seconds}") long expireSeconds
    ) {
        byte[] raw;
        try {
            raw = Decoders.BASE64.decode(secret);
        } catch (Exception ignore) {
            raw = secret.getBytes(StandardCharsets.UTF_8);
        }
        if (raw.length < 32) {
            raw = Arrays.copyOf(raw, 32);
        }
        this.key = Keys.hmacShaKeyFor(raw);
        this.expireSeconds = expireSeconds;
    }

    public String generateToken(AuthUser authUser) {
        Instant now = Instant.now();
        Instant expiry = now.plusSeconds(expireSeconds);

        return Jwts.builder()
            .subject(String.valueOf(authUser.id()))
            .claim("uid", authUser.id())
            .claim("phone", authUser.phone())
            .claim("role", authUser.role())
            .claim("realName", authUser.realName())
            .issuedAt(Date.from(now))
            .expiration(Date.from(expiry))
            .signWith(key)
            .compact();
    }

    public AuthUser parseToken(String token) {
        Claims claims = Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
        Long uid = claims.get("uid", Long.class);
        String phone = claims.get("phone", String.class);
        String role = claims.get("role", String.class);
        String realName = claims.get("realName", String.class);
        return new AuthUser(uid, phone, role, realName);
    }

    public Instant getExpireAt() {
        return Instant.now().plusSeconds(expireSeconds);
    }
}
