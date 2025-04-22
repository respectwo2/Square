package org.shax3.square.domain.auth.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Date;

import static jakarta.persistence.GenerationType.IDENTITY;
import static lombok.AccessLevel.PROTECTED;

@Entity
@Getter
@NoArgsConstructor(access = PROTECTED)
@AllArgsConstructor(access = PROTECTED)
@Builder
@Table(
        name = "refresh_token"
)
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "token", nullable = false)
    private String token;

    @Column(name = "expiry", nullable = false)
    private Date expiry;

    public static RefreshToken createRefreshToken(Long userId, String refreshToken, Date expiry) {
        return RefreshToken.builder()
                .userId(userId)
                .token(refreshToken)
                .expiry(expiry)
                .build();
    }

    public void reissueRefreshToken(RefreshToken newRefreshToken) {
        this.token = newRefreshToken.getToken();
        this.expiry = newRefreshToken.getExpiry();
    }
}
