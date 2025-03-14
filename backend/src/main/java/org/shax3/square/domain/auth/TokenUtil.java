package org.shax3.square.domain.auth;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.shax3.square.domain.auth.domain.RefreshToken;
import org.shax3.square.domain.auth.dto.UserTokenDto;
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

    public TokenUtil(
            @Value("${spring.auth.jwt.secret-key}") final String secretKey,
            @Value("${spring.auth.jwt.access-token-expiry}") final Long accessTokenExpiry,
            @Value("${spring.auth.jwt.refresh-token-expiry}") final Long refreshTokenExpiry

    ) {
        this.secretKey = Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
        this.accessTokenExpiry = accessTokenExpiry;
        this.refreshTokenExpiry = refreshTokenExpiry;
    }

    public UserTokenDto createLoginToken(Long subject) {
        String accessToken = createAccessToken(subject.toString(), accessTokenExpiry);
        String refreshToken = generateRefreshToken();
        RefreshToken userRefreshToken = RefreshToken.createRefreshToken(
                subject,
                refreshToken,
                new Date(System.currentTimeMillis() + refreshTokenExpiry)
        );
        return new UserTokenDto(accessToken, userRefreshToken);
    }

    private String createAccessToken(String subject, Long expiredMs) {
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
}