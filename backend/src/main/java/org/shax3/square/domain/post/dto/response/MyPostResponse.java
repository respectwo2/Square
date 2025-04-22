package org.shax3.square.domain.post.dto.response;

import org.shax3.square.domain.post.dto.PostSummaryDto;

import java.util.List;

public record MyPostResponse(
        List<PostSummaryDto> posts,
        Long nextCursorId
) {
}
