package com.wbbb.steam.util;

import com.wbbb.steam.entity.User;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Token处理工具
 */
public class TokenUtil {
    private static final String key = "THE_PAST_IS_NEVER_DEAD_IT'S_NOT_EVEN_PAST";

    /**
     * 根据账户信息生成Token
     * @param user 账户信息
     * @return token
     */
    public static String generateToken(User user, boolean rememberMe) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", user.getUserId());
        claims.put("username", user.getUsername());
        claims.put("role", user.getRole()); 
        claims.put("enabled", user.getEnabled());
        long expiration = rememberMe ? 30 * 24 * 60 * 60 * 1000L : 2 * 60 * 60 * 1000L;
        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(Keys.hmacShaKeyFor(key.getBytes(StandardCharsets.UTF_8)), SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * 验证并解析Token
     * @param token token
     * @return token有效返回账户id，无效返回null
     */
    public static Long parseToken(String token) {
        if (token == null || token.isEmpty())
            return null;
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(Keys.hmacShaKeyFor(key.getBytes(StandardCharsets.UTF_8)))
                    .build()
                    .parseClaimsJws(token)
                    .getBody()
                    .get("userId", Long.class);
        } catch (JwtException e) {
            return null;
        }
    }
}
