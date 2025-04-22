package org.shax3.square.domain.type.dto.response;

import java.util.List;

public record TypeTestQuestionResponse(
        List<TypeTestQuestion> questions
) {
    public static TypeTestQuestionResponse from(List<TypeTestQuestion> questions) {
        return new TypeTestQuestionResponse(questions);
    }
}
