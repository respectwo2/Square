package org.shax3.square.domain.like.dto;

import jakarta.validation.constraints.NotNull;
import org.shax3.square.common.model.TargetType;
import org.shax3.square.domain.like.model.Like;
import org.shax3.square.domain.user.model.User;

public record LikeRequest(
        @NotNull
        Long targetId,

        @NotNull
        TargetType targetType
) {
    public Like to(User user) {
        return Like.builder()
                .user(user)
                .targetId(targetId)
                .targetType(targetType)
                .build();
    }
}
