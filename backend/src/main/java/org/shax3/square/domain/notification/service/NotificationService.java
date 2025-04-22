package org.shax3.square.domain.notification.service;

import lombok.RequiredArgsConstructor;
import org.shax3.square.domain.notification.dto.response.NotificationResponseDto;
import org.shax3.square.domain.notification.model.Notification;
import org.shax3.square.domain.notification.model.NotificationType;
import org.shax3.square.domain.notification.repository.NotificationRepository;
import org.shax3.square.domain.opinion.service.OpinionService;
import org.shax3.square.domain.user.model.User;
import org.shax3.square.domain.user.service.UserDeviceService;
import org.shax3.square.domain.user.service.UserService;
import org.shax3.square.exception.CustomException;
import org.shax3.square.exception.ExceptionCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final FcmService fcmService;
    private final UserDeviceService userDeviceService;
    private final UserService userService;
    private final OpinionService opinionService;


    public void createNotification(User receiver, String title, String message, NotificationType type, Long targetId) {
        Notification notification = Notification.builder()
                .user(receiver)
                .title(title)
                .message(message)
                .type(type)
                .targetId(targetId)
                .build();

        notificationRepository.save(notification);
        List<String> tokens = userDeviceService.getFcmTokensByUser(receiver);
        Map<String, String> data = new HashMap<>();
        data.put("type", type.name());
        data.put("targetId", String.valueOf(targetId));


        if (type == NotificationType.DEBATE_COMMENT) {
            Long debateId = opinionService.findDebateIdByOpinionId(targetId);
            if (debateId != null) {
                data.put("debateId", String.valueOf(debateId));
            }
        }


        for (String token : tokens) {
            fcmService.sendPush(token, title, message, data);
        }
    }

    @Transactional
    public void markAsRead(Long notificationId, Long userId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new CustomException(ExceptionCode.NOTIFICATION_NOT_FOUND));

        if (!notification.getUser().getId().equals(userId)) {
            throw new CustomException(ExceptionCode.UNAUTHORIZED_ACCESS);
        }

        notification.markAsRead();
    }

    // 전체 읽음 처리
    @Transactional
    public void markAllAsRead(Long userId) {
        User user = userService.findById(userId);
        List<Notification> notifications = notificationRepository.findAllByUser(user);
        notifications.forEach(Notification::markAsRead);
    }

    @Transactional(readOnly = true)
    public List<NotificationResponseDto> getNotifications(Long userId) {
        User user = userService.findById(userId);

        List<Notification> notifications = notificationRepository.findAllByUserOrderByCreatedAtDesc(user);

        return notifications.stream()
                .map(NotificationResponseDto::from)
                .toList();
    }
}
