package org.shax3.square.domain.s3.dto.response;

public record PresignedPutUrlResponse(
        String presignedPutUrl,
        String s3Key
) {
    public static PresignedPutUrlResponse of(String presignedPutUrl, String s3Key) {
        return new PresignedPutUrlResponse(presignedPutUrl, s3Key);
    }
}
