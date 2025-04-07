package org.shax3.square.domain.auth.service;

import com.google.firebase.auth.FirebaseToken;
import lombok.RequiredArgsConstructor;
import org.shax3.square.domain.auth.TokenUtil;
import org.shax3.square.domain.auth.dto.request.FirebaseLoginRequest;
import org.shax3.square.domain.auth.model.RefreshToken;
import org.shax3.square.domain.auth.dto.UserLoginDto;
import org.shax3.square.domain.auth.dto.UserTokenDto;
import org.shax3.square.domain.auth.repository.RefreshTokenRepository;
import org.shax3.square.domain.user.model.SocialType;
import org.shax3.square.domain.user.model.User;
import org.shax3.square.domain.user.repository.UserRepository;
import org.shax3.square.exception.CustomException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.shax3.square.exception.ExceptionCode.*;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final TokenUtil tokenUtil;
    private final GoogleAuthService googleAuthService;


    @Transactional
    public UserLoginDto loginTest(String email) {
        Optional<User> user = userRepository.findByEmail(email);

        if (user.isPresent()) {
            User loginUser = user.get();

            Long userId = loginUser.getId();
            UserTokenDto userTokens = tokenUtil.createLoginToken(userId);
            refreshTokenRepository.deleteByUserId(userId);
            refreshTokenRepository.save(userTokens.refreshToken());

            return UserLoginDto.createMemberLoginDto(userTokens, loginUser);
        }

        return UserLoginDto.createNotMemberLoginDto(email);
    }

    @Transactional
    public UserLoginDto googleLogin(String code) {

        String email = googleAuthService.googleCallback(code);

        Optional<User> user = userRepository.findByEmail(email);

        if (user.isPresent()) {
            User loginUser = user.get();
            checkSocialType(loginUser.getSocialType(), SocialType.GOOGLE);

            Long userId = loginUser.getId();
            UserTokenDto userTokens = tokenUtil.createLoginToken(userId);
            refreshTokenRepository.deleteByUserId(userId);
            refreshTokenRepository.save(userTokens.refreshToken());

            return UserLoginDto.createMemberLoginDto(userTokens, loginUser);
        }

        return UserLoginDto.createNotMemberLoginDto(email);
    }

    @Transactional
    public UserTokenDto reissueTokens(String refreshToken, String authHeader) {
        String accessToken = authHeader.split(" ")[1];

        if (tokenUtil.isTokenValid(accessToken)) {
            Long userId = Long.parseLong(tokenUtil.getSubject(accessToken));
            RefreshToken existingRefreshToken = refreshTokenRepository.findByUserId(userId)
                    .orElseThrow(() -> new CustomException(INVALID_REFRESH_TOKEN));

            return new UserTokenDto(accessToken, existingRefreshToken);
        }


        Long userId = tokenUtil.isAccessTokenExpired(accessToken);
        if (userId != null) {
            RefreshToken foundRefreshToken = refreshTokenRepository.findByUserId(userId)
                    .orElseThrow(() -> new CustomException(INVALID_REFRESH_TOKEN));

            validateRefreshToken(refreshToken, foundRefreshToken.getToken());

            UserTokenDto newTokens = tokenUtil.createLoginToken(userId);
            foundRefreshToken.reissueRefreshToken(newTokens.refreshToken());

            return newTokens;
        }

        throw new CustomException(FAILED_TO_VALIDATE_TOKEN);
    }

    private void validateRefreshToken(String refreshToken, String foundRefreshToken) {
        if (refreshToken.equals(foundRefreshToken)) {
            return;
        }
        throw new CustomException(INVALID_REQUEST);
    }

    private void checkSocialType (SocialType userSocialType, SocialType loginType) {
        if (userSocialType == loginType) {
            return;
        }
        throw new CustomException(SOCIAL_TYPE_MISMATCH);
    }

    @Transactional
    public void logout(String refreshToken) {
        refreshTokenRepository.deleteByToken(refreshToken);
    }

    @Transactional
    public UserLoginDto firebaseLogin(FirebaseToken token, FirebaseLoginRequest request) {
        String email = token.getEmail();

        Optional<User> user = userRepository.findByEmail(email);

        if (user.isPresent()) {
            User loginUser = user.get();
            checkSocialType(loginUser.getSocialType(), request.socialType());

            Long userId = loginUser.getId();
            UserTokenDto userTokens = tokenUtil.createLoginToken(userId);
            refreshTokenRepository.deleteByUserId(userId);
            refreshTokenRepository.save(userTokens.refreshToken());

            return UserLoginDto.createMemberLoginDto(userTokens, loginUser);
        }

        // 비회원 처리
        return UserLoginDto.createNotMemberLoginDto(email);
    }

}
