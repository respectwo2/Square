package org.shax3.square.domain.post.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record UpdatePostRequest(
        @NotBlank
        String title,
        @NotBlank
        String content,
        @NotNull
        List<String> deletedImages,
        @NotNull
        List<String> addedImages
) {
}
