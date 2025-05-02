package org.shax3.square.domain.auth.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import okhttp3.*;
import org.shax3.square.exception.CustomException;
import org.shax3.square.exception.ExceptionCode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
@Service
@RequiredArgsConstructor
public class KakaoAuthService {

    @Value("${oauth2.kakao.client-id}")
    private String kakaoClientId;

    @Value("${oauth2.kakao.redirect-uri}")
    private String kakaoRedirectUri;

    @Value("${oauth2.kakao.client-secret}")
    private String kakaoClientSecret;

    private static final String TOKEN_URL = "https://kauth.kakao.com/oauth/token";
    private static final String USER_INFO_URL = "https://kapi.kakao.com/v2/user/me";

    private final OkHttpClient client = new OkHttpClient();

    public String kakakoCallback(String code) {
        RequestBody body = new FormBody.Builder()
                .add("code", code)
                .add("client_id", kakaoClientId)
                .add("redirect_uri", kakaoRedirectUri)
                .add("client_secret", kakaoClientSecret)
                .add("grant_type", "authorization_code")
                .build();

        Request tokenRequest = new Request.Builder()
                .url(TOKEN_URL)
                .post(body)
                .build();

        String accessToken;
        try (Response response = client.newCall(tokenRequest).execute()) {
            String responseBody = response.body().string();
            ObjectMapper mapper = new ObjectMapper();
            JsonNode json = mapper.readTree(responseBody);
            accessToken = json.get("access_token").asText();
        } catch (IOException e) {
            throw new CustomException(ExceptionCode.UNABLE_TO_GET_ACCESS_TOKEN);
        }

        Request userInfoRequest = new Request.Builder()
                .url(USER_INFO_URL)
                .addHeader("Authorization", "Bearer " + accessToken)
                .build();

        try (Response response = client.newCall(userInfoRequest).execute()) {
            String bodyStr = response.body().string();
            ObjectMapper mapper = new ObjectMapper();
            JsonNode userInfo = mapper.readTree(bodyStr);
            return userInfo.get("kakao_account").get("email").asText();
        } catch (IOException e) {
            throw new CustomException(ExceptionCode.UNABLE_TO_GET_USER_INFO);
        }
    }
}

