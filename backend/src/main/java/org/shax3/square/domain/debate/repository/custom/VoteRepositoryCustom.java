package org.shax3.square.domain.debate.repository.custom;

import org.shax3.square.domain.debate.model.Vote;
import org.shax3.square.domain.user.model.User;

import java.util.List;

public interface VoteRepositoryCustom {
    List<Vote> findByUserOrderByIdDesc(User user, Long nextCursorId, int limit);

    List<Vote> findByUserAndDebateIds(User user, List<Long> debateIds);
}

