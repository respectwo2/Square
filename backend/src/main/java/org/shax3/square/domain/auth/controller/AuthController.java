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

        Cookie cookie = new Cookie("refresh-token", userLoginDto.refreshToken().getToken());
        cookie.setHttpOnly(true);
        cookie.setSecure(false);
        cookie.setPath("/");
        response.addCookie(cookie);

        return ResponseEntity.ok().body(TestUserInfoResponse.from(userLoginDto, userLoginDto.accessToken(), userLoginDto.refreshToken().getToken()));
    }

//    @Operation(
//            summary = "임시 로그인 API2",
//            description = "email을 입력하면 Access Token과 Refresh Token을 반환합니다."
//    )
//    @GetMapping("/test2")
//    public ResponseEntity<TestUserInfoResponse> loginTest2(
//            HttpServletResponse response
//    ) {
//        UserLoginDto userLoginDto = authService.loginTest("test2@test.com");
//
//        Cookie cookie = new Cookie("refresh-token", userLoginDto.refreshToken().getToken());
//        cookie.setHttpOnly(true);
//        cookie.setSecure(false);
//        cookie.setPath("/");
//        response.addCookie(cookie);
//
//        return ResponseEntity.ok().body(TestUserInfoResponse.from(userLoginDto, userLoginDto.accessToken(), userLoginDto.refreshToken().getToken()));
//    }
//
//    @Operation(
//            summary = "임시 로그인 API3",
//            description = "email을 입력하면 Access Token과 Refresh Token을 반환합니다."
//    )
//    @GetMapping("/test3")
//    public ResponseEntity<TestUserInfoResponse> loginTest3(
//            HttpServletResponse response
//    ) {
//        UserLoginDto userLoginDto = authService.loginTest("test3@test.com");
//
//        Cookie cookie = new Cookie("refresh-token", userLoginDto.refreshToken().getToken());
//        cookie.setHttpOnly(true);
//        cookie.setSecure(false);
//        cookie.setPath("/");
//        response.addCookie(cookie);
//
//        return ResponseEntity.ok().body(TestUserInfoResponse.from(userLoginDto, userLoginDto.accessToken(), userLoginDto.refreshToken().getToken()));
//    }

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
    public ResponseEntity<UserInfoResponse> redirectGoogle(
            @RequestParam("code") String code,
            HttpServletResponse response
    ) {
        UserLoginDto userLoginDto = authService.googleLogin(code);

        if (userLoginDto.refreshToken() != null) {
            Cookie cookie = new Cookie("refresh-token", userLoginDto.refreshToken().getToken());
            cookie.setHttpOnly(true);
            cookie.setSecure(false);
            cookie.setPath("/");
            response.addCookie(cookie);
            response.setHeader("Authorization", "Bearer " + userLoginDto.accessToken());

            return ResponseEntity.ok().body(UserInfoResponse.from(userLoginDto));
        }

        String email = userLoginDto.email();
        String signUpToken = tokenUtil.createSignUpToken(email, "GOOGLE");

        Cookie cookie = new Cookie("sign-up-token", signUpToken);
        cookie.setHttpOnly(true);
        cookie.setSecure(false);
        cookie.setPath("/");
        response.addCookie(cookie);

        return ResponseEntity.ok().body(UserInfoResponse.from(userLoginDto));
    }

    @Operation(
            summary = "엑세스 토큰 재발급 api",
            description = "엑세스 토큰을 재발급해 header에 넣어줍니다."
    )
    @PostMapping("/reissue")
    public ResponseEntity<Void> reissueToken(
            @CookieValue("refresh-token") String refreshToken,
            @RequestHeader("Authorization") String authHeader,
            HttpServletResponse response
    ) {
        UserTokenDto tokenDto = authService.reissueTokens(refreshToken, authHeader);

        response.setHeader("Authorization", "Bearer " + tokenDto.accessToken());

        Cookie cookie = new Cookie("refresh-token", tokenDto.refreshToken().getToken());
        cookie.setHttpOnly(true);
        cookie.setSecure(false);
        cookie.setPath("/");
        response.addCookie(cookie);


        return ResponseEntity.ok().build();
    }

    @Operation(
            summary = "로그아웃 api",
            description = "쿠키를 지워주고 refreshToken을 만료시킵니다."
    )
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            @CookieValue("refresh-token") String refreshToken,
            HttpServletResponse response
    ) {
        authService.logout(refreshToken);

        Cookie cookie = new Cookie("refresh-token", null);
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        cookie.setMaxAge(0);

        response.addCookie(cookie);

        return ResponseEntity.ok().build();
    }

    @PostMapping("/firebase")
    public ResponseEntity<FirebaseLoginResponse> loginWithFirebase(
            @RequestBody FirebaseLoginRequest request,
            HttpServletResponse response
    ) {
        FirebaseToken firebaseUser = firebaseService.verifyIdToken(request.idToken());
        UserLoginDto userLoginDto = authService.firebaseLogin(firebaseUser, request);
        if (userLoginDto.isMember()) {

            Cookie cookie = new Cookie("refresh-token", userLoginDto.refreshToken().getToken());
            cookie.setHttpOnly(true);
            cookie.setSecure(false);
            cookie.setPath("/");
            response.addCookie(cookie);

            userDeviceService.registerOrUpdateDevice(
                    userLoginDto.userId(),
                    request.fcmToken(),
                    request.deviceId(),
                    request.deviceType()
            );

            return ResponseEntity.ok(FirebaseLoginResponse.member(userLoginDto));
        }


        return ResponseEntity.ok(FirebaseLoginResponse.notMember(userLoginDto.email(), userLoginDto.socialType()));
    }


}