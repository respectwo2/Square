package org.shax3.square.domain.post.dto;

import org.shax3.square.domain.post.model.PostComment;

public record MyCommentDto(
        Long commentId,
        Long postId,
        String title,
        String content,
        int likeCount,
        boolean isLiked
) {
    public static MyCommentDto from(PostComment comment, boolean isLiked, int likeCount) {
        return new MyCommentDto(
                comment.getId(),
                comment.getPost().getId(),
                comment.getPost().getTitle(),
                comment.getContent(),
                likeCount,
                isLiked
        );
    }
}
