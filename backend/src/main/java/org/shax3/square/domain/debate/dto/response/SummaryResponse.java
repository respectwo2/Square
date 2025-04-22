package org.shax3.square.domain.debate.dto.response;

import org.shax3.square.domain.debate.dto.SummaryDto;

import java.util.List;

public record SummaryResponse(
        Integer leftCount,
        Integer rightCount,
        List<SummaryDto> summaries
) {
    public static SummaryResponse of(
            VoteResponse voteResponse,
            List<SummaryDto> summaries) {

        Integer leftCount = (voteResponse != null) ? voteResponse.leftCount() : null;
        Integer rightCount = (voteResponse != null) ? voteResponse.rightCount() : null;


        return new SummaryResponse(
                leftCount,
                rightCount,
                summaries
        );
    }
}

