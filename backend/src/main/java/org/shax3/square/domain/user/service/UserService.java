package org.shax3.square.domain.user.service;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.shax3.square.domain.auth.TokenUtil;
import org.shax3.square.domain.auth.dto.UserLoginDto;
import org.shax3.square.domain.auth.dto.UserTokenDto;
import org.shax3.square.domain.auth.repository.RefreshTokenRepository;
import org.shax3.square.domain.s3.service.S3Service;
import org.shax3.square.domain.user.dto.request.CheckNicknameRequest;
import org.shax3.square.domain.user.dto.request.SignUpRequest;
import org.shax3.square.domain.user.dto.request.UpdateProfileRequest;
import org.shax3.square.domain.user.dto.response.CheckNicknameResponse;
import org.shax3.square.domain.user.dto.response.ProfileInfoResponse;
import org.shax3.square.domain.user.dto.response.ProfileUrlResponse;
import org.shax3.square.domain.user.dto.response.UserChoiceResponse;
import org.shax3.square.domain.user.model.AgeRange;
import org.shax3.square.domain.user.model.User;
import org.shax3.square.domain.user.repository.UserRedisRepository;
import org.shax3.square.domain.user.repository.UserRepository;
import org.shax3.square.exception.CustomException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.shax3.square.exception.ExceptionCode.*;

@Service
@RequiredArgsConstructor
public class UserService {

    private final TokenUtil tokenUtil;
    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRedisRepository userRedisRepository;

    private static final String DEFAULT_PROFILE_IMG = "profile/0c643827-c958-465b-875d-918c8a22fe01.png";
    private final S3Service s3Service;

    @Transactional
    public UserLoginDto signUp(SignUpRequest signUpRequest) {
        Optional<User> foundUser = userRepository.findByEmail(signUpRequest.email());
        if (foundUser.isPresent()) {
            throw new CustomException(DUPLICATE_EMAIL);
        }

        String s3Key = signUpRequest.s3Key();
        if (s3Key == null) {
            s3Key = DEFAULT_PROFILE_IMG;
        }
        if (!s3Key.startsWith("profile/")) {
            throw new CustomException(NOT_PROFILE_IMG);
        }

        AgeRange ageRange = calculateAgeRange(signUpRequest.yearOfBirth());
        User signUpUser = signUpRequest.to(ageRange, s3Key);
        userRepository.save(signUpUser);

        UserTokenDto userTokens = tokenUtil.createLoginToken(signUpUser.getId());
        refreshTokenRepository.save(userTokens.refreshToken());

        return UserLoginDto.createMemberLoginDto(userTokens, signUpUser);
    }

    private AgeRange calculateAgeRange(int yearOfBirth) {
        int age = LocalDate.now().getYear() - yearOfBirth;
        age /= 10;

        return switch (age) {
            case 0 -> throw new CustomException(AGE_LIMIT_FROM_TEN);
            case 1 -> AgeRange.TEN;
            case 2 -> AgeRange.TWENTY;
            case 3 -> AgeRange.THIRTY;
            case 4 -> AgeRange.FORTY;
            default -> AgeRange.FIFTY;
        };
    }

    public UserChoiceResponse getUserChoices() {
        return UserChoiceResponseFactory.getUserChoiceResponse();
    }

    @Transactional
    public void deleteAccount(User user, String refreshToken) {
        refreshTokenRepository.deleteByToken(refreshToken);
        User foundUser = userRepository.findById(user.getId())
                .orElseThrow(() -> new CustomException(USER_NOT_FOUND));
        foundUser.deleteAccount();
    }

    @Transactional(readOnly = true)
    public CheckNicknameResponse checkNicknameDuplication(@Valid CheckNicknameRequest checkNicknameRequest) {
        String nickname = checkNicknameRequest.nickname();
        Optional<User> user = userRepository.findByNickname(nickname);
        if (user.isPresent()) {
            return CheckNicknameResponse.canCreate(false);
        }

        if (!userRedisRepository.reserveNickname(nickname)) {
            return CheckNicknameResponse.canCreate(false);
        }
        return CheckNicknameResponse.canCreate(true);
    }

    public ProfileInfoResponse getProfileInfo(User user) {
        String profileUrl = s3Service.generatePresignedGetUrl(user.getS3Key());
        return ProfileInfoResponse.of(user, profileUrl);
    }

    @Transactional
    public ProfileUrlResponse updateProfileInfo(User user, UpdateProfileRequest updateProfileRequest) {
        User foundUser = userRepository.findById(user.getId())
                .orElseThrow(() -> new CustomException(USER_NOT_FOUND));

        String s3Key = updateProfileRequest.s3Key();
        if (s3Key == null) {
            s3Key = foundUser.getS3Key();
        }
        foundUser.updateProfileInfo(updateProfileRequest, s3Key);

        String profileUrl = s3Service.generatePresignedGetUrl(s3Key);
        return ProfileUrlResponse.from(profileUrl);
    }

    public List<User> findAllById(List<Long> userIds) {
        return userRepository.findAllById(userIds);
    }

    public User findById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(USER_NOT_FOUND));
    }

    public User findByNickname(String nickname) {

        return userRepository.findByNickname(nickname)
                .orElseThrow(() -> new CustomException(USER_NOT_FOUND));
    }

    public List<User> findAll() {
        return userRepository.findAll();
    }
}
