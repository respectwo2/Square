package org.shax3.square.domain.post.repository.custom;

import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.shax3.square.common.model.TargetType;
import org.shax3.square.domain.like.model.QLike;
import org.shax3.square.domain.post.model.Post;
import org.shax3.square.domain.post.model.QPost;
import org.shax3.square.domain.scrap.model.QScrap;
import org.shax3.square.domain.user.model.QUser;
import org.shax3.square.domain.user.model.User;

import java.util.List;

@RequiredArgsConstructor
public class PostRepositoryImpl implements PostRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<Post> findPopularPosts(int count) {
        QPost post = QPost.post;

        return queryFactory
                .selectFrom(post)
                .where(post.valid.isTrue())
                .orderBy(post.likeCount.desc())
                .limit(count)
                .fetch();
    }

    @Override
    public List<Post> findByLatestCursor(Long cursorId, int limit) {
        QPost post = QPost.post;
        QUser user = QUser.user;

        return queryFactory
                .selectFrom(post)
                .join(post.user, user).fetchJoin()
                .where(
                        post.valid.isTrue(),
                        cursorId != null ? post.id.lt(cursorId) : null
                )
                .orderBy(post.id.desc())
                .limit(limit)
                .fetch();
    }

    @Override
    public List<Post> findByLikesCursor(Integer cursorLikes, Long cursorId, int limit) {
        QPost post = QPost.post;
        QUser user = QUser.user;

        return queryFactory
                .selectFrom(post)
                .join(post.user, user).fetchJoin()
                .where(
                        post.valid.isTrue(),
                        cursorLikes != null && cursorId != null
                                ? post.likeCount.lt(cursorLikes)
                                .or(post.likeCount.eq(cursorLikes).and(post.id.lt(cursorId)))
                                : null
                )
                .orderBy(post.likeCount.desc(), post.id.desc())
                .limit(limit)
                .fetch();
    }

    @Override
    public List<Post> findMyPosts(User user, Long cursorId, int limit) {
        QPost post = QPost.post;

        return queryFactory
                .selectFrom(post)
                .where(
                        post.valid.isTrue(),
                        post.user.eq(user),
                        cursorId != null ? post.id.lt(cursorId) : null
                )
                .orderBy(post.id.desc())
                .limit(limit + 1)
                .fetch();
    }

    @Override
    public List<Post> findMyLikedPosts(User user, Long cursorId, int limit) {
        QPost post = QPost.post;
        QLike like = QLike.like1;

        return queryFactory
                .select(post)
                .from(like)
                .join(post).on(like.targetId.eq(post.id))
                .where(
                        post.valid.isTrue(),
                        like.user.eq(user),
                        like.targetType.eq(TargetType.POST),
                        like.like.isTrue(),
                        cursorId != null ? post.id.lt(cursorId) : null
                )
                .orderBy(post.id.desc())
                .limit(limit)
                .fetch();
    }

    @Override
    public List<Post> findMyScrapPosts(User user, Long cursorId, int limit) {
        QPost post = QPost.post;
        QScrap scrap = QScrap.scrap;

        return queryFactory
                .select(post)
                .from(scrap)
                .join(post).on(scrap.targetId.eq(post.id))
                .where(
                        post.valid.isTrue(),
                        scrap.user.eq(user),
                        cursorId != null ? post.id.lt(cursorId) : null
                )
                .orderBy(post.id.desc())
                .limit(limit)
                .fetch();
    }

}
