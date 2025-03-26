package org.shax3.square.domain.opinion.dto;

import org.shax3.square.domain.opinion.model.Opinion;

public record MyOpinionDto(
        Long opinionId,
        String topic,
        String content,
        int likeCount,
        boolean isLiked
) {

    public static MyOpinionDto from(Opinion opinion,boolean isLiked) {
        if (opinion == null) {
            return null;
        }
        return new MyOpinionDto(
                opinion.getId(),
                opinion.getDebate().getTopic(),
                opinion.getContent(),
                opinion.getLikeCount(),
                isLiked
        );
    }

}
