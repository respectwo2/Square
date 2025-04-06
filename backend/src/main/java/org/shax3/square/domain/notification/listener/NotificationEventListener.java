package org.shax3.square.domain.notification.listener;

import org.shax3.square.domain.notification.event.DebateCommentCreatedEvent;
import org.shax3.square.domain.notification.event.NoticePublishedEvent;
import org.shax3.square.domain.notification.event.PostCommentCreatedEvent;
import org.shax3.square.domain.notification.event.TodayDebateStartedEvent;
import org.shax3.square.domain.notification.model.NotificationType;
import org.shax3.square.domain.notification.service.NotificationService;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class NotificationEventListener {

	private final NotificationService notificationService;

	@EventListener
	public void handlePostComment(PostCommentCreatedEvent event) {
		notificationService.createNotification(
			event.receiver(),
			"새로운 댓글 알림",
			event.commentContent(),
			NotificationType.POST_COMMENT,
			event.postId()
		);
	}

	@EventListener
	public void handleDebateComment(DebateCommentCreatedEvent event) {
		notificationService.createNotification(
			event.receiver(),
			"새로운 답글 알림",
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
