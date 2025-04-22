package org.shax3.square.domain.notification.event;

import org.shax3.square.domain.user.model.User;

public record TodayDebateStartedEvent(
        User receiver,
        String debateTopic,
        Long debateId
) {
}
