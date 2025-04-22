package org.shax3.square.domain.post.dto;

import org.shax3.square.domain.post.model.Post;

import java.time.LocalDateTime;

public record PopularPostDto(
        Long postId,
        String title,
        LocalDateTime createdAt,
        int likeCount,
        int commentCount
) {
    public static PopularPostDto from(Post post, int commentCount, int likeCount) {
        return new PopularPostDto(
                post.getId(),
                post.getTitle(),
                post.getCreatedAt(),
                likeCount,
                commentCount
        );
    }
}