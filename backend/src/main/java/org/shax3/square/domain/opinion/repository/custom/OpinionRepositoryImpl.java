package org.shax3.square.domain.opinion.repository.custom;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.shax3.square.domain.opinion.model.Opinion;
import org.shax3.square.domain.opinion.model.QOpinion;
import org.shax3.square.domain.user.model.User;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class OpinionRepositoryImpl implements OpinionRepositoryCustom {
    private final JPAQueryFactory queryFactory;

    @Override
    public List<Opinion> findMyOpinions(User user, Long nextCursorId, int limit) {
        QOpinion opinion = QOpinion.opinion;
        BooleanBuilder builder = new BooleanBuilder();

        builder.and(opinion.user.eq(user));

        if (nextCursorId != null) {
            builder.and(opinion.id.lt(nextCursorId));
        }

        builder.and(opinion.valid.isTrue());

        return queryFactory
                .selectFrom(opinion)
                .join(opinion.user).fetchJoin()
                .where(builder)
                .orderBy(opinion.id.desc())
                .limit(limit)
                .fetch();
    }

    @Override
    public List<Opinion> findOpinionsByLatest(Long debateId, boolean isLeft, Long nextCursorId, int limit) {
        QOpinion op = QOpinion.opinion;
        BooleanBuilder builder = new BooleanBuilder();

        builder.and(op.debate.id.eq(debateId));
        builder.and(op.left.eq(isLeft));
        builder.and(op.valid.isTrue());

        if (nextCursorId != null) {
            builder.and(op.id.lt(nextCursorId));
        }

        return queryFactory
                .selectFrom(op)
                .join(op.user).fetchJoin()
                .where(builder)
                .orderBy(op.id.desc())
                .limit(limit)
                .fetch();
    }

    @Override
    public List<Opinion> findOpinionsByLikes(Long debateId, boolean isLeft, Long nextCursorId, Integer nextCursorLikes, int limit) {
        QOpinion op = QOpinion.opinion;
        BooleanBuilder builder = new BooleanBuilder();

        builder.and(op.debate.id.eq(debateId));
        builder.and(op.left.eq(isLeft));
        builder.and(op.valid.isTrue());

        if (nextCursorLikes != null && nextCursorId != null) {
            builder.and(
                    op.likeCount.lt(nextCursorLikes)
                            .or(op.likeCount.eq(nextCursorLikes).and(op.id.lt(nextCursorId)))
            );
        }

        return queryFactory
                .selectFrom(op)
                .join(op.user).fetchJoin()
                .where(builder)
                .orderBy(op.likeCount.desc(), op.id.desc())
                .limit(limit)
                .fetch();
    }

    @Override
    public List<Opinion> findOpinionsByComments(Long debateId, boolean isLeft, Long nextCursorId, Integer nextCursorComments, int limit) {
        QOpinion op = QOpinion.opinion;
        BooleanBuilder builder = new BooleanBuilder();

        builder.and(op.debate.id.eq(debateId));
        builder.and(op.left.eq(isLeft));
        builder.and(op.valid.isTrue());

        if (nextCursorComments != null && nextCursorId != null) {
            builder.and(
                    op.commentCount.lt(nextCursorComments)
                            .or(op.commentCount.eq(nextCursorComments).and(op.id.lt(nextCursorId)))
            );
        }

        return queryFactory
                .selectFrom(op)
                .join(op.user).fetchJoin()
                .where(builder)
                .orderBy(op.commentCount.desc(), op.id.desc())
                .limit(limit)
                .fetch();
    }
}
