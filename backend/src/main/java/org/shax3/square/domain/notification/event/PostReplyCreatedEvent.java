package org.shax3.square.domain.notification.event;

import org.shax3.square.domain.user.model.User;

public record PostReplyCreatedEvent(
        User receiver,
        String replyContent,
        Long postId
) {
}
