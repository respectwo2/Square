package org.shax3.square.domain.post.dto;

import org.shax3.square.domain.post.model.PostComment;

import java.time.LocalDateTime;

public record ReplyDto(
        Long replyId,
        Long parentId,
        String nickname,
        String profileUrl,
        String userType,
        LocalDateTime createdAt,
        String content,
        int likeCount,
        boolean isLiked
) {
    public static ReplyDto from(PostComment reply, String profileUrl, boolean isLiked, int likeCount) {
        return new ReplyDto(
                reply.getId(),
                reply.getParent().getId(),
                reply.getUser().getNickname(),
                profileUrl,
                reply.getUser().getType().name(),
                reply.getCreatedAt(),
                reply.getContent(),
                likeCount,
                isLiked
        );
    }
}
