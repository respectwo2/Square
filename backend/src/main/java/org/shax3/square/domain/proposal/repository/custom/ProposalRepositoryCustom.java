package org.shax3.square.domain.proposal.repository.custom;

import org.shax3.square.domain.proposal.model.Proposal;

import java.util.List;

public interface ProposalRepositoryCustom {
    List<Proposal> findProposalsByLatest(Long nextCursorId, int limit);

    List<Proposal> findProposalsByLikes(Long nextCursorId, Integer nextCursorLikes, int limit);
}
