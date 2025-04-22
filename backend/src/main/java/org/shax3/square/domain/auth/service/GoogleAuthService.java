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
public class GoogleAuthService {

    @Value("${oauth2.google.redirect-uri}")
    private String googleRedirectUri;

    @Value("${oauth2.google.client-id}")
    private String googleClientId;

    @Value("${oauth2.google.client-secret}")
    private String googleClientSecret;

    private static final String TOKEN_URL = "https://oauth2.googleapis.com/token";

    private static final String USER_INFO_URL = "https://www.googleapis.com/oauth2/v2/userinfo";

    private final OkHttpClient client = new OkHttpClient();

    public String googleCallback(String code) {
        RequestBody requestBody = new FormBody.Builder()
                .add("code", code)
                .add("client_id", googleClientId)
                .add("client_secret", googleClientSecret)
                .add("redirect_uri", googleRedirectUri)
                .add("grant_type", "authorization_code")
                .build();

        Request request = new Request.Builder()
                .url(TOKEN_URL)
                .post(requestBody)
                .build();

        String accessToken;
        try {
            Response response = client.newCall(request).execute();
            String responseBody = response.body().string();
            ObjectMapper mapper = new ObjectMapper();
            JsonNode jsonNode = mapper.readTree(responseBody);
            accessToken = jsonNode.get("access_token").asText();
        } catch (IOException e) {
            throw new CustomException(ExceptionCode.UNABLE_TO_GET_ACCESS_TOKEN);
        }

        Request userInfoRequest = new Request.Builder()
                .url(USER_INFO_URL)
                .addHeader("Authorization", "Bearer " + accessToken)
                .build();

        String email;
        try {
            Response userInfoResponse = client.newCall(userInfoRequest).execute();
            String userInfo = userInfoResponse.body().string();
            ObjectMapper mapper = new ObjectMapper();
            JsonNode jsonNode = mapper.readTree(userInfo);
            email = jsonNode.get("email").asText();
        } catch (IOException e) {
            throw new CustomException((ExceptionCode.UNABLE_TO_GET_USER_INFO));
        }

        return email;
    }
}
