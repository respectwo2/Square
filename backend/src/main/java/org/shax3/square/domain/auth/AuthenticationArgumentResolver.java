package org.shax3.square.domain.auth;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.shax3.square.domain.auth.annotation.AuthUser;
import org.shax3.square.domain.user.model.User;
import org.shax3.square.domain.user.repository.UserRepository;
import org.shax3.square.exception.CustomException;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import java.util.Arrays;

import static org.shax3.square.domain.user.model.State.INACTIVE;
import static org.shax3.square.domain.user.model.State.LEAVE;
import static org.shax3.square.exception.ExceptionCode.*;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;

@Component
@RequiredArgsConstructor
@Slf4j
public class AuthenticationArgumentResolver implements HandlerMethodArgumentResolver {
    private final UserRepository userRepository;
    private final TokenUtil tokenUtil;

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(AuthUser.class);
    }

    @Override
    public Object resolveArgument(MethodParameter parameter,
                                  ModelAndViewContainer mavContainer,
                                  NativeWebRequest webRequest,
                                  WebDataBinderFactory binderFactory) {
        HttpServletRequest request = webRequest.getNativeRequest(HttpServletRequest.class);

        if (request == null) {
            throw new CustomException(FAILED_TO_VALIDATE_TOKEN);
        }

        String refreshToken = extractRefreshToken(request);
        String accessToken = extractAccessToken(request);

        if (tokenUtil.isTokenValid(accessToken)) {
            User user = extractUser(accessToken);
            checkState(user);
            return user;
        }

        throw new CustomException(FAILED_TO_VALIDATE_TOKEN);
    }

    private String extractAccessToken(HttpServletRequest request) {
        String authHeader = request.getHeader(AUTHORIZATION);
        if (authHeader == null) {
            throw new CustomException(INVALID_ACCESS_TOKEN);
        }
        return authHeader.split(" ")[1];
    }

    private String extractRefreshToken(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();

        if (cookies == null) {
            throw new CustomException(INVALID_REFRESH_TOKEN);
        }

        return Arrays.stream(cookies)
                .filter(cookie -> cookie.getName().equals("refresh-token"))
                .findFirst()
                .orElseThrow(() -> new CustomException(INVALID_REFRESH_TOKEN))
                .getValue();
    }

    private User extractUser(String accessToken) {
        Long userId = Long.valueOf(tokenUtil.getSubject(accessToken));

        return userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(INVALID_REQUEST));
    }

    private void checkState(User user) {
        if (user.getState() == LEAVE) {
            throw new CustomException(USER_DELETED);
        }
        if (user.getState() == INACTIVE) {
            throw new CustomException(USER_INACTIVE);
        }
    }
}
