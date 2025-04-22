package org.shax3.square.domain.post.dto.response;

import org.shax3.square.domain.post.dto.CommentDto;
import org.shax3.square.domain.post.dto.PostImageDto;
import org.shax3.square.domain.post.model.Post;

import java.time.LocalDateTime;
import java.util.List;

public record PostDetailResponse(
        Long postId,
        List<PostImageDto> images,
        String profileUrl,
        String userType,
        String nickname,
        LocalDateTime createdAt,
        String title,
        String content,
        int likeCount,
        int commentCount,
        boolean isLiked,
        boolean isScrapped,
        List<CommentDto> comments
) {
    public static PostDetailResponse from(
            Post post,
            List<PostImageDto> images,
            String profileUrl,
            boolean isLiked,
            boolean isScrapped,
            List<CommentDto> comments,
            int commentCount,
            int likeCount
    ) {
        return new PostDetailResponse(
                post.getId(),
                images,
                profileUrl,
                post.getUser().getType().name(),
                post.getUser().getNickname(),
                post.getCreatedAt(),
                post.getTitle(),
                post.getContent(),
                likeCount,
                commentCount,
                isLiked,
                isScrapped,
                comments
        );
    }
}
