package org.shax3.square.domain.auth.controller;

import com.google.firebase.auth.FirebaseToken;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.shax3.square.domain.auth.TokenUtil;
import org.shax3.square.domain.auth.dto.UserLoginDto;
import org.shax3.square.domain.auth.dto.UserTokenDto;
import org.shax3.square.domain.auth.dto.request.FirebaseLoginRequest;
import org.shax3.square.domain.auth.dto.response.FirebaseLoginResponse;
import org.shax3.square.domain.auth.dto.response.TestUserInfoResponse;
import org.shax3.square.domain.auth.dto.response.UserInfoResponse;
import org.shax3.square.domain.auth.service.AuthService;
import org.shax3.square.domain.auth.service.FirebaseService;
import org.shax3.square.domain.user.service.UserDeviceService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "Auth", description = "인증 관련 API")
public class AuthController {

    @Value("${oauth2.google.redirect-uri}")
    private String googleRedirectUri;

    @Value("${oauth2.google.client-id}")
    private String googleClientId;

    @Value("${KAKAO_REDIRECT_URI}")
    private String kakaoRedirectUri;

    @Value("${KAKAO_CLIENT_ID")
    private String kakaoClientId;

    private final AuthService authService;
    private final TokenUtil tokenUtil;
    private final UserDeviceService userDeviceService;
    private final FirebaseService firebaseService;

    @Operation(
            summary = "임시 로그인 API",
            description = "email을 입력하면 Access Token과 Refresh Token을 반환합니다."
    )
    @GetMapping("/test1")
    public ResponseEntity<TestUserInfoResponse> loginTest1(
            HttpServletResponse response
    ) {
        UserLoginDto userLoginDto = authService.loginTest("test@test.com");

        setCookie(response, "refresh-token", userLoginDto.refreshToken().getToken());

        return ResponseEntity.ok().body(TestUserInfoResponse.from(userLoginDto, userLoginDto.accessToken(), userLoginDto.refreshToken().getToken()));
    }


    @Operation(
            summary = "구글 로그인 API",
            description = "구글 로그인으로 연결되는 api입니다."
    )
    @GetMapping("/google")
    public void loginWithGoogle(HttpServletResponse response) throws IOException {
        String encodedRedirectUri = URLEncoder.encode(googleRedirectUri, StandardCharsets.UTF_8);
        String googleAuthUrl = "https://accounts.google.com/o/oauth2/v2/auth"
                + "?client_id=" + googleClientId
                + "&redirect_uri=" + encodedRedirectUri
                + "&response_type=code"
                + "&scope=email"
                + "&access_type=offline";

        response.sendRedirect(googleAuthUrl);
    }

    @GetMapping("/callback/google")
    public ResponseEntity<UserInfoResponse> redirectGoogle(@RequestParam("code") String code, HttpServletResponse response) {
        return handleSocialCallback(authService.googleLogin(code), "GOOGLE", response);
    }

    @GetMapping("/kakao")
    public void loginWithKakao(HttpServletResponse response) throws IOException {
        String kakaoAuthUrl = "https://kauth.kakao.com/oauth/authorize"
                + "?client_id=" + kakaoClientId
                + "&redirect_uri=" + kakaoRedirectUri
                + "&response_type=code";
        response.sendRedirect(kakaoAuthUrl);
    }

    @GetMapping("/callback/kakao")
    public ResponseEntity<UserInfoResponse> redirectKakao(@RequestParam("code") String code, HttpServletResponse response) {
        return handleSocialCallback(authService.kakaoLogin(code), "KAKAO", response);
    }

    private ResponseEntity<UserInfoResponse> handleSocialCallback(UserLoginDto userLoginDto, String socialType, HttpServletResponse response) {
        UserInfoResponse userInfo = UserInfoResponse.from(userLoginDto);

        if (userLoginDto.refreshToken() != null) {
            setCookie(response, "refresh-token", userLoginDto.refreshToken().getToken());
            expireCookie(response, "sign-up-token");
            response.setHeader("Authorization", "Bearer " + userLoginDto.accessToken());
        } else {
            String signUpToken = tokenUtil.createSignUpToken(userLoginDto.email(), socialType);
            setCookie(response, "sign-up-token", signUpToken);
            expireCookie(response, "refresh-token");
        }

        return ResponseEntity.ok(userInfo);
    }

    @PostMapping("/reissue")
    @Operation(summary = "엑세스 토큰 재발급 api", description = "엑세스 토큰을 재발급해 header에 넣어줍니다.")
    public ResponseEntity<Void> reissueToken(@CookieValue("refresh-token") String refreshToken,
                                             @RequestHeader("Authorization") String authHeader,
                                             HttpServletResponse response) {
        UserTokenDto tokenDto = authService.reissueTokens(refreshToken, authHeader);
        response.setHeader("Authorization", "Bearer " + tokenDto.accessToken());
        setCookie(response, "refresh-token", tokenDto.refreshToken().getToken());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/logout")
    @Operation(summary = "로그아웃 api", description = "쿠키를 지워주고 refreshToken을 만료시킵니다.")
    public ResponseEntity<Void> logout(@CookieValue("refresh-token") String refreshToken, HttpServletResponse response) {
        authService.logout(refreshToken);
        expireCookie(response, "refresh-token");
        return ResponseEntity.ok().build();
    }

    @PostMapping("/firebase")
    public ResponseEntity<FirebaseLoginResponse> loginWithFirebase(@RequestBody FirebaseLoginRequest request,
                                                                   HttpServletResponse response) {
        FirebaseToken firebaseUser = firebaseService.verifyIdToken(request.idToken());
        UserLoginDto userLoginDto = authService.firebaseLogin(firebaseUser, request);

        if (userLoginDto.isMember()) {
            setCookie(response, "refresh-token", userLoginDto.refreshToken().getToken());
            userDeviceService.registerOrUpdateDevice(userLoginDto.userId(), request.fcmToken(), request.deviceId(), request.deviceType());
            return ResponseEntity.ok(FirebaseLoginResponse.member(userLoginDto));
        }

        return ResponseEntity.ok(FirebaseLoginResponse.notMember(userLoginDto.email(), userLoginDto.socialType()));
    }

    private void setCookie(HttpServletResponse response, String name, String value) {
        Cookie cookie = new Cookie(name, value);
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setPath("/");
        response.addCookie(cookie);
    }

    private void expireCookie(HttpServletResponse response, String name) {
        Cookie cookie = new Cookie(name, null);
        cookie.setPath("/");
        cookie.setMaxAge(0);
        response.addCookie(cookie);
    }

}