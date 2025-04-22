package org.shax3.square.domain.post.dto.response;

import org.shax3.square.domain.post.dto.MyCommentDto;

import java.util.List;

public record MyCommentListResponse(
        List<MyCommentDto> comments,
        Long nextCursorId
) {
}
