package com.maotuanzi.security.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * JWT 工具
 */
@Slf4j
@Component
public class JwtUtil {

    @Value("${maotuanzi.jwt.secret}")
    private String secret;

    @Value("${maotuanzi.jwt.access-expire:604800}")
    private long accessExpire;

    @Value("${maotuanzi.jwt.refresh-expire:2592000}")
    private long refreshExpire;

    private SecretKey getKey() {
        byte[] bytes = secret.getBytes(StandardCharsets.UTF_8);
        if (bytes.length < 32) {
            byte[] padded = new byte[32];
            System.arraycopy(bytes, 0, padded, 0, bytes.length);
            bytes = padded;
        }
        return Keys.hmacShaKeyFor(bytes);
    }

    public String generateAccess(Long userId, String openid, Integer vipLevel) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("uid", userId);
        claims.put("openid", openid);
        claims.put("vip", vipLevel);
        claims.put("type", "access");
        return buildToken(claims, accessExpire);
    }

    public String generateRefresh(Long userId) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("uid", userId);
        claims.put("type", "refresh");
        return buildToken(claims, refreshExpire);
    }

    private String buildToken(Map<String, Object> claims, long expireSeconds) {
        long now = System.currentTimeMillis();
        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(new Date(now))
                .setExpiration(new Date(now + expireSeconds * 1000L))
                .signWith(getKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public Claims parse(String token) {
        Jws<Claims> jws = Jwts.parserBuilder()
                .setSigningKey(getKey())
                .build()
                .parseClaimsJws(token);
        return jws.getBody();
    }

    /** Admin tokens use the configured signing key, just like the parser. */
    public String generateAdmin(Long adminId, java.util.Collection<String> permissions, long expireSeconds) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("adminId", adminId);
        claims.put("perms", permissions);
        claims.put("type", "admin");
        return buildToken(claims, expireSeconds);
    }

    public boolean isValid(String token) {
        try {
            parse(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public long getAccessExpire() {
        return accessExpire;
    }

    public long getRefreshExpire() {
        return refreshExpire;
    }
}
