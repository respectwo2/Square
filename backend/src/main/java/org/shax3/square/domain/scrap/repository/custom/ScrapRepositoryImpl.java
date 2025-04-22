package org.shax3.square.domain.scrap.repository.custom;

import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.shax3.square.common.model.TargetType;
import org.shax3.square.domain.scrap.model.QScrap;
import org.shax3.square.domain.scrap.model.Scrap;
import org.shax3.square.domain.user.model.User;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class ScrapRepositoryImpl implements ScrapRepositoryCustom {
    private final JPAQueryFactory queryFactory;

    @Override
    public List<Scrap> findScrapsByUserAndType(User user, TargetType type, Long cursorId, int limit) {

        QScrap scrap = QScrap.scrap;

        return queryFactory
                .selectFrom(scrap)
                .where(
                        scrap.user.eq(user),
                        scrap.targetType.eq(type),
                        cursorId != null ? scrap.id.lt(cursorId) : null
                )
                .orderBy(scrap.id.desc())
                .limit(limit)
                .fetch();
    }
}

