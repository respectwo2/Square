package org.shax3.square.domain.scrap.dto.request;

import jakarta.validation.constraints.NotNull;
import org.shax3.square.common.model.TargetType;
import org.shax3.square.domain.scrap.model.Scrap;
import org.shax3.square.domain.user.model.User;

public record CreateScrapRequest(
        @NotNull
        Long targetId,
        @NotNull
        TargetType targetType
) {
    public Scrap to(User user) {
        return Scrap.builder()
                .user(user)
                .targetId(targetId)
                .targetType(targetType)
                .build();
    }
}
