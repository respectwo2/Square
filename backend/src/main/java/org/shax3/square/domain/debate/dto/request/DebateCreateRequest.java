package org.shax3.square.domain.debate.dto.request;

import jakarta.validation.constraints.NotBlank;
import org.shax3.square.domain.debate.model.Category;
import org.shax3.square.domain.debate.model.Debate;

public record DebateCreateRequest(
        Long proposalId,

        @NotBlank(message = "주제를 입력해주세요.")
        String topic,

        @NotBlank(message = "왼쪽 의견을 입력해주세요.")
        String leftOption,

        @NotBlank(message = "오른쪽 의견을 입력해주세요.")
        String rightOption,

        @NotBlank(message = "카테고리 이름을 입력해주세요.")
        String categoryName
) {
    public Debate to(Category category) {
        return Debate.builder()
                .topic(topic)
                .leftOption(leftOption)
                .rightOption(rightOption)
                .category(category)
                .build();
    }
}
