package org.shax3.square.domain.notification.event;

import org.shax3.square.domain.user.model.User;

public record PostCommentCreatedEvent(
        User receiver,
        String commentContent,
        Long postId
) {
}
