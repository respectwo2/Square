package org.shax3.square.domain.auth.dto;

import lombok.Builder;
import org.shax3.square.domain.auth.model.RefreshToken;
import org.shax3.square.domain.user.model.SocialType;
import org.shax3.square.domain.user.model.State;
import org.shax3.square.domain.user.model.Type;
import org.shax3.square.domain.user.model.User;

@Builder
public record UserLoginDto(
        Long userId,
        String accessToken,
        RefreshToken refreshToken,
        String email,
        String nickname,
        Type userType,
        State state,
        SocialType socialType,
        boolean isMember
) {
    public static UserLoginDto createMemberLoginDto(UserTokenDto userTokenDto, User user) {
        return UserLoginDto.builder()
                .userId(user.getId())
                .accessToken(userTokenDto.accessToken())
                .refreshToken(userTokenDto.refreshToken())
                .email(null)
                .nickname(user.getNickname())
                .userType(user.getType())
                .state(user.getState())
                .socialType((user.getSocialType()))
                .isMember(true)
                .build();
    }

    public static UserLoginDto createNotMemberLoginDto(String email) {
        return UserLoginDto.builder()
                .userId(null)
                .accessToken(null)
                .refreshToken(null)
                .email(email)
                .nickname(null)
                .userType(null)
                .state(null)
                .socialType(null)
                .isMember(false)
                .build();
    }
}
