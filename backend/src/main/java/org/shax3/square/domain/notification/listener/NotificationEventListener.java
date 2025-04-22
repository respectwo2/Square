package org.shax3.square.domain.notification.listener;

import lombok.RequiredArgsConstructor;
import org.shax3.square.domain.notification.event.*;
import org.shax3.square.domain.notification.model.NotificationType;
import org.shax3.square.domain.notification.service.NotificationService;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;


@Component
@RequiredArgsConstructor
public class NotificationEventListener {

    private final NotificationService notificationService;

    @EventListener
    public void handlePostComment(PostCommentCreatedEvent event) {
        notificationService.createNotification(
                event.receiver(),
                "내 게시물에 댓글이 달렸어요",
                event.commentContent(),
                NotificationType.POST_COMMENT,
                event.postId()
        );
    }

    @EventListener
    public void handlePostReply(PostReplyCreatedEvent event) {
        notificationService.createNotification(
                event.receiver(),
                "내 댓글에 답글이 달렸어요",
                event.replyContent(),
                NotificationType.POST_COMMENT,
                event.postId()
        );
    }

    @EventListener
    public void handleDebateComment(DebateCommentCreatedEvent event) {
        notificationService.createNotification(
                event.receiver(),
                "내 의견에 답글이 달렸어요",
                event.commentContent(),
                NotificationType.DEBATE_COMMENT,
                event.opinionId()
        );
    }


    @EventListener
    public void handleTodayDebate(TodayDebateStartedEvent event) {
        notificationService.createNotification(
                event.receiver(),
                "오늘의 논쟁이 시작됐어요",
                event.debateTopic(),
                NotificationType.TODAY_DEBATE,
                event.debateId()
        );
    }

    @EventListener
    public void handleNotice(NoticePublishedEvent event) {
        notificationService.createNotification(
                event.receiver(),
                event.noticeTitle(),
                event.noticeContent(),
                NotificationType.NOTICE,
                null
        );
    }
}
