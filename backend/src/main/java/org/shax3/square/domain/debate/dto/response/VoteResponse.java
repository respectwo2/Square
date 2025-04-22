package org.shax3.square.domain.debate.dto.response;

public record VoteResponse(
        int leftCount,
        int rightCount
) {
    public static VoteResponse of(int leftCount, int rightCount) {
        return new VoteResponse(leftCount, rightCount);
    }

    public static VoteResponse create() {
        return new VoteResponse(0, 0);
    }
}
