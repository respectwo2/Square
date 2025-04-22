package org.shax3.square.domain.auth.dto.response;

import lombok.Builder;
import org.shax3.square.domain.auth.dto.UserLoginDto;
import org.shax3.square.domain.user.model.SocialType;
import org.shax3.square.domain.user.model.Type;

@Builder
public record FirebaseLoginResponse(
        boolean isMember,
        String email,
        SocialType socialType,
        String nickname,
        Type userType,
        String accessToken,
        String refreshToken
) {
    //가입했다면 이메일 필요 없음
    public static FirebaseLoginResponse member(UserLoginDto dto) {
        return FirebaseLoginResponse.builder()
                .isMember(true)
                .email(null)
                .socialType(null)
                .nickname(dto.nickname())
                .userType(dto.userType())
                .accessToken(dto.accessToken())
                .refreshToken(dto.refreshToken().getToken())
                .build();
    }

    //가입해야한다면 이메일이 필요함
    public static FirebaseLoginResponse notMember(String email, SocialType socialType) {
        return FirebaseLoginResponse.builder()
                .isMember(false)
                .email(email)
                .socialType(socialType)
                .nickname(null)
                .userType(null)
                .socialType(null)
                .accessToken(null)
                .refreshToken(null)
                .build();
    }
}