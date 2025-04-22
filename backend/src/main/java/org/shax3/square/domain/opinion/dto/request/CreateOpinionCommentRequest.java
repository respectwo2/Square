package org.shax3.square.domain.opinion.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.shax3.square.domain.opinion.model.Opinion;
import org.shax3.square.domain.opinion.model.OpinionComment;
import org.shax3.square.domain.user.model.User;

public record CreateOpinionCommentRequest(
        @NotNull
        Long opinionId,

        @NotBlank(message = "댓글 내용은 비어 있을 수 없습니다.")
        @Size(min = 5, max = 150, message = "댓글은 최소 5자 이상, 최대 150자 이하이어야 합니다.")
        String content
) {

    public OpinionComment to(Opinion opinion, User user) {
        return OpinionComment.builder()
                .opinion(opinion)
                .user(user)
                .content(content)
                .build();
    }
}
