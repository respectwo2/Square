package org.shax3.square.domain.opinion.dto.response;

import org.shax3.square.domain.opinion.model.OpinionComment;

import java.time.LocalDateTime;

public record CommentResponse(
        Long commentId,
        String nickname,
        String profileUrl,
        String userType,
        LocalDateTime createdAt,
        int likeCount,
        String content,
        boolean isLiked
) {
    public static CommentResponse of(OpinionComment comment, String profileUrl) {
        return new CommentResponse(
                comment.getId(),
                comment.getUser().getNickname(),
                profileUrl,
                comment.getUser().getType().name(),
                comment.getCreatedAt(),
                comment.getLikeCount(),
                comment.getContent(),
                false // TODO: 좋아요 여부 로직 추가 필요
        );
    }
}
