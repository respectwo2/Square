package org.shax3.square.domain.post.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdatePostCommetRequest(

        @NotBlank(message = "댓글 내용은 비어 있을 수 없습니다.")
        @Size(min = 5, max = 150, message = "댓글은 최소 5자 이상, 최대 150자 이하이어야 합니다.")
        String content

) {

}
