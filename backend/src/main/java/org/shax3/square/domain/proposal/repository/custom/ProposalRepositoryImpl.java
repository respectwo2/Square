package org.shax3.square.domain.proposal.repository.custom;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.shax3.square.domain.proposal.model.Proposal;
import org.shax3.square.domain.proposal.model.QProposal;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class ProposalRepositoryImpl implements ProposalRepositoryCustom {
    private final JPAQueryFactory queryFactory;

    public List<Proposal> findProposalsByLatest(Long nextCursorId, int limit) {
        QProposal proposal = QProposal.proposal;
        BooleanBuilder builder = new BooleanBuilder();

        if (nextCursorId != null) {
            builder.and(proposal.id.lt(nextCursorId));
        }

        builder.and(proposal.valid.isTrue());

        return queryFactory
                .selectFrom(proposal)
                .where(builder)
                .orderBy(proposal.id.desc())
                .limit(limit)
                .fetch();
    }


    @Override
    public List<Proposal> findProposalsByLikes(Long nextCursorId, Integer nextCursorLikes, int limit) {
        QProposal proposal = QProposal.proposal;
        BooleanBuilder builder = new BooleanBuilder();

        if (nextCursorLikes != null) {
            builder.and(
                    proposal.likeCount.lt(nextCursorLikes)
                            .or(proposal.likeCount.eq(nextCursorLikes)
                                    .and(proposal.id.lt(nextCursorId)))
            );
        }
        builder.and(proposal.valid.isTrue());

        return queryFactory
                .selectFrom(proposal)
                .where(builder)
                .orderBy(proposal.likeCount.desc(), proposal.id.desc())
                .limit(limit)
                .fetch();
    }

}
