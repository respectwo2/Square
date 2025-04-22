package org.shax3.square.domain.post.dto.response;

import org.shax3.square.domain.post.dto.PopularPostDto;
import org.shax3.square.domain.post.dto.PostSummaryDto;

import java.util.List;

public record PostListResponse(
        String userType,
        List<PopularPostDto> popularPosts,
        List<PostSummaryDto> posts,
        Long nextCursorId,
        Integer nextCursorLikes
) {
    public static PostListResponse of(
            String userType,
            List<PopularPostDto> popularPosts,
            List<PostSummaryDto> posts,
            Long nextCursorId,
            Integer nextCursorLikes
    ) {
        return new PostListResponse(userType, popularPosts, posts, nextCursorId, nextCursorLikes);
    }
}
