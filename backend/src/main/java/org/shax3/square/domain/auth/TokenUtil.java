package org.shax3.square.domain.auth;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.shax3.square.domain.auth.dto.UserTokenDto;
import org.shax3.square.domain.auth.model.RefreshToken;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Date;

@Component
public class TokenUtil {

    private final SecretKey secretKey;
    private final Long accessTokenExpiry;
    private final Long refreshTokenExpiry;
    private final Long signUpTokenExpiry;

    public TokenUtil(
            @Value("${spring.auth.jwt.secret-key}") final String secretKey,
            @Value("${spring.auth.jwt.access-token-expiry}") final Long accessTokenExpiry,
            @Value("${spring.auth.jwt.refresh-token-expiry}") final Long refreshTokenExpiry,
            @Value("${spring.auth.jwt.sign-up-token-expiry}") final Long signUpTokenExpiry
    ) {
        this.secretKey = Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
        this.accessTokenExpiry = accessTokenExpiry;
        this.refreshTokenExpiry = refreshTokenExpiry;
        this.signUpTokenExpiry = signUpTokenExpiry;
    }

    public UserTokenDto createLoginToken(Long subject) {
        String accessToken = createToken(subject.toString(), accessTokenExpiry);
        String refreshToken = generateRefreshToken();
        RefreshToken userRefreshToken = RefreshToken.createRefreshToken(
                subject,
                refreshToken,
                new Date(System.currentTimeMillis() + refreshTokenExpiry)
        );
        return new UserTokenDto(accessToken, userRefreshToken);
    }

    public String createSignUpToken(String email, String socialType) {
        String subject = email + ":" + socialType;

        return createToken(subject, signUpTokenExpiry);
    }

    private String createToken(String subject, Long expiredMs) {
        return Jwts.builder()
                .setSubject(subject) // 주체 (유저 ID 또는 이메일 등)
                .setIssuedAt(new Date(System.currentTimeMillis())) // 발급 시간
                .setExpiration(new Date(System.currentTimeMillis() + expiredMs)) // 만료 시간
                .signWith(secretKey) // 서명
                .compact();
    }

    private String generateRefreshToken() {
        SecureRandom secureRandom = new SecureRandom();
        byte[] tokenBytes = new byte[32];
        secureRandom.nextBytes(tokenBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(tokenBytes);
    }

    public boolean isTokenValid(String accessToken) {
        try {
            parseToken(accessToken);
        } catch (JwtException e) {
            return false;
        }
        return true;
    }

    public Long isAccessTokenExpired(String accessToken) {
        try {
            parseToken(accessToken);
        } catch (ExpiredJwtException e) {
            return Long.parseLong(e.getClaims().getSubject());
        } catch (JwtException e) {
            return null;
        }
        return null;
    }

    private Jws<Claims> parseToken(String accessToken) {
        return Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(accessToken);
    }

    public String getSubject(String accessToken) {
        return parseToken(accessToken).getBody().getSubject();
    }
}