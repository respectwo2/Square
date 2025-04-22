package org.shax3.square.domain.opinion.repository.custom;

import org.shax3.square.domain.opinion.model.Opinion;
import org.shax3.square.domain.user.model.User;

import java.util.List;

public interface OpinionRepositoryCustom {
    List<Opinion> findMyOpinions(User user, Long nextCursorId, int limit);

    List<Opinion> findOpinionsByLatest(Long debateId, boolean isLeft, Long nextCursorId, int limit);

    List<Opinion> findOpinionsByLikes(Long debateId, boolean isLeft, Long nextCursorId, Integer nextCursorLikes, int limit);

    List<Opinion> findOpinionsByComments(Long debateId, boolean isLeft, Long nextCursorId, Integer nextCursorComments, int limit);

}