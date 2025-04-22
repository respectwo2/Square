package org.shax3.square.domain.opinion.dto.response;

import org.shax3.square.domain.opinion.dto.MyOpinionDto;

import java.util.List;

public record MyOpinionResponse(
        List<MyOpinionDto> opinions,
        Long nextCursorId
) {
    public static MyOpinionResponse of(List<MyOpinionDto> opinionDtos, Long newNextCursorId) {

        return new MyOpinionResponse(opinionDtos, newNextCursorId);
    }
}


