package org.shax3.square.domain.like.repository.custom;

import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.shax3.square.common.model.TargetType;
import org.shax3.square.domain.like.model.QLike;
import org.shax3.square.domain.user.model.User;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@RequiredArgsConstructor
public class LikeRepositoryImpl implements LikeRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Set<Long> findLikedTargetIds(User user, TargetType targetType, List<Long> targetIds) {
        QLike like = QLike.like1;

        return new HashSet<>(
                queryFactory.select(like.targetId)
                        .from(like)
                        .where(
                                like.user.eq(user),
                                like.targetType.eq(targetType),
                                like.targetId.in(targetIds),
                                like.like.isTrue()
                        )
                        .fetch()
        );
    }
}
