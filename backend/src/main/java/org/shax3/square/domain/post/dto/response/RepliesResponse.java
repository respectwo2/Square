package org.shax3.square.domain.post.dto.response;

import org.shax3.square.domain.post.dto.ReplyDto;

import java.util.List;

public record RepliesResponse(
        List<ReplyDto> replies,
        Long nextCursorId
) {
}
