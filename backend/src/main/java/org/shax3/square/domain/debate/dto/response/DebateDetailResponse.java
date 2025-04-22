package org.shax3.square.domain.debate.dto.response;

import org.shax3.square.domain.opinion.dto.OpinionDto;

import java.util.List;

public record DebateDetailResponse(
        int leftCount,
        int rightCount,
        List<OpinionDto> opinions,
        Long nextLeftCursorId,
        Integer nextLeftCursorLikes,
        Integer nextLeftCursorComments,
        Long nextRightCursorId,
        Integer nextRightCursorLikes,
        Integer nextRightCursorComments
) {
    public static DebateDetailResponse of(
            List<OpinionDto> opinions,
            Long nextLeftCursorId,
            Integer nextLeftCursorLikes,
            Integer nextLeftCursorComments,
            Long nextRightCursorId,
            Integer nextRightCursorLikes,
            Integer nextRightCursorComments,
            VoteResponse voteResponse
    ) {
        return new DebateDetailResponse(
                voteResponse.leftCount(),
                voteResponse.rightCount(),
                opinions,
                nextLeftCursorId,
                nextLeftCursorLikes,
                nextLeftCursorComments,
                nextRightCursorId,
                nextRightCursorLikes,
                nextRightCursorComments
        );
    }
}
