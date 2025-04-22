package org.shax3.square.domain.debate.dto;

public record DebateVotedResultResponse(
        VoteResultDto leftResult,
        VoteResultDto rightResult
) {
}
