package org.shax3.square.domain.auth.dto.request;

import org.shax3.square.domain.user.model.SocialType;

public record FirebaseLoginRequest(
        String idToken,
        String fcmToken,
        String deviceId,
        String deviceType,
        SocialType socialType
) {
}