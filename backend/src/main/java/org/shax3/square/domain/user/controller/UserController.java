package org.shax3.square.domain.user.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.shax3.square.domain.auth.annotation.AuthUser;
import org.shax3.square.domain.auth.dto.UserLoginDto;
import org.shax3.square.domain.auth.dto.response.FirebaseLoginResponse;
import org.shax3.square.domain.user.dto.request.CheckNicknameRequest;
import org.shax3.square.domain.user.dto.request.SignUpRequest;
import org.shax3.square.domain.user.dto.request.UpdateProfileRequest;
import org.shax3.square.domain.user.dto.response.CheckNicknameResponse;
import org.shax3.square.domain.user.dto.response.ProfileInfoResponse;
import org.shax3.square.domain.user.dto.response.ProfileUrlResponse;
import org.shax3.square.domain.user.dto.response.UserChoiceResponse;
import org.shax3.square.domain.user.model.User;
import org.shax3.square.domain.user.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/users")
@RequiredArgsConstructor
@Tag(name = "User", description = "유저 관련 API")
public class UserController {

    private final UserService userService;

    @Operation(
            summary = "회원가입 API",
            description = "회원가입을 위한 정보를 보내주시면 AccessToken과 RefreshToken을 발급해줍니다"
    )
    @PostMapping
    public ResponseEntity<FirebaseLoginResponse> userSignUp(
            HttpServletResponse response,
            @Valid @RequestBody SignUpRequest signUpRequest
    ) {
        UserLoginDto userLoginDto = userService.signUp(signUpRequest);

        Cookie cookie = new Cookie("refresh-token", userLoginDto.refreshToken().getToken());
        cookie.setHttpOnly(true);
        cookie.setSecure(false);
        cookie.setPath("/");
        response.addCookie(cookie);

        return ResponseEntity.ok(FirebaseLoginResponse.member(userLoginDto));
    }

    @Operation(
            summary = "유저 탈퇴 api",
            description = "유저를 탈퇴처리 시키고 refreshToken을 만료시킵니다."
    )
    @DeleteMapping
    public ResponseEntity<Void> deleteAccount(
            @AuthUser User user,
            @CookieValue("refresh-token") String refreshToken,
            HttpServletResponse response
    ) {
        userService.deleteAccount(user, refreshToken);

        Cookie cookie = new Cookie("refresh-token", null);
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        cookie.setMaxAge(0);

        response.addCookie(cookie);

        return ResponseEntity.ok().build();
    }

    @Operation(
            summary = "프로필 정보 조회 api",
            description = "프로필 사진, 지역, 종교에 관한 정보를 제공합니다."
    )
    @GetMapping
    public ResponseEntity<ProfileInfoResponse> getProfileInfo(
            @AuthUser User user
    ) {
        ProfileInfoResponse profileInfoResponse = userService.getProfileInfo(user);

        return ResponseEntity.ok(profileInfoResponse);
    }

    @Operation(
            summary = "프로필 정보 수정 api",
            description = "닉네임, 프로필 사진, 지역, 종교를 수정하면 프로필 사진 url을 반환합니다."
    )
    @PutMapping
    public ResponseEntity<ProfileUrlResponse> updateProfileInfo(
            @AuthUser User user,
            @Valid @RequestBody UpdateProfileRequest updateProfileRequest
    ) {
        ProfileUrlResponse profileUrlResponse = userService.updateProfileInfo(user, updateProfileRequest);

        return ResponseEntity.ok(profileUrlResponse);
    }

    @Operation(
            summary = "닉네임 중복 조회 api",
            description = "닉네임이 중복되는지 확인하여 true/false를 반환합니다."
    )
    @PostMapping("/nickname")
    public ResponseEntity<CheckNicknameResponse> checkNickname(
            @Valid @RequestBody CheckNicknameRequest checkNicknameRequest
    ) {
        CheckNicknameResponse checkNicknameResponse = userService.checkNicknameDuplication(checkNicknameRequest);
        return ResponseEntity.ok(checkNicknameResponse);
    }

    @Operation(
            summary = "유저정보 선택지 조회 api",
            description = "회원가입 또는 회원 정보 수정 시 필요한 선택지를 제공합니다."
    )
    @GetMapping("/choices")
    public ResponseEntity<UserChoiceResponse> getChoices() {
        UserChoiceResponse userChoiceResponse = userService.getUserChoices();
        return ResponseEntity.ok(userChoiceResponse);
    }
}
