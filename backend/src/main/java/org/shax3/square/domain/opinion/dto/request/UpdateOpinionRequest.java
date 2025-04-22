package org.shax3.square.domain.opinion.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateOpinionRequest(
        @NotBlank(message = "의견은 비어 있을 수 없습니다.")
        @Size(min = 10, max = 150, message = "의견은 최소 10자 이상, 최대 150자 이하이어야 합니다.")
        String content) {
}
