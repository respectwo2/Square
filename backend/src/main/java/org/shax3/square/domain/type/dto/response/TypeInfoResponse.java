package org.shax3.square.domain.type.dto.response;

public record TypeInfoResponse(
        String nickname,
        String userType,
        int score1,
        int score2,
        int score3,
        int score4
) {
    public static TypeInfoResponse of(String nickname, String userType, int[] score) {
        return new TypeInfoResponse(
                nickname,
                userType,
                score[0],
                score[1],
                score[2],
                score[3]
        );
    }
}
