package org.shax3.square.domain.notification.service;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import lombok.RequiredArgsConstructor;
import org.shax3.square.exception.CustomException;
import org.shax3.square.exception.ExceptionCode;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class FcmService {

    public void sendPush(String fcmToken, String title, String body, Map<String, String> data) {
        Message.Builder builder = Message.builder()
                .setToken(fcmToken)
                .setNotification(
                        com.google.firebase.messaging.Notification.builder()
                                .setTitle(title)
                                .setBody(body)
                                .build()
                )
                .putAllData(data);

        try {
            FirebaseMessaging.getInstance().send(builder.build());
        } catch (FirebaseMessagingException e) {

            throw new CustomException(ExceptionCode.FIREBASE_MESSAGE_ERROR);
        }
    }
}
