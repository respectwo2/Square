package org.shax3.square.domain.post.repository.custom;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.shax3.square.domain.post.model.Post;
import org.shax3.square.domain.post.model.PostComment;
import org.shax3.square.domain.post.model.QPost;
import org.shax3.square.domain.post.model.QPostComment;
import org.shax3.square.domain.user.model.QUser;
import org.shax3.square.domain.user.model.User;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class PostCommentRepositoryImpl implements PostCommentRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Map<Long, Integer> countCommentsByPostIds(List<Long> postIds) {

        QPostComment comment = QPostComment.postComment;

        return queryFactory
                .select(comment.post.id, comment.count())
                .from(comment)
                .where(comment.valid.isTrue(), comment.post.id.in(postIds))
                .groupBy(comment.post.id)
                .fetch()
                .stream()
                .collect(Collectors.toMap(
                        tuple -> tuple.get(comment.post.id), // key: postId
                        tuple -> {
                            Long count = tuple.get(comment.count()); // value: comment count
                            return count == null ? 0 : Math.toIntExact(count);
                        })
                );
    }

    @Override
    public Map<Long, Integer> countRepliesByParentIds(List<Long> parentIds) {
        QPostComment comment = QPostComment.postComment;

        return queryFactory
                .select(comment.parent.id, comment.count())
                .from(comment)
                .where(comment.valid.isTrue(), comment.parent.id.in(parentIds))
                .groupBy(comment.parent.id)
                .fetch()
                .stream()
                .collect(Collectors.toMap(
                        tuple -> tuple.get(comment.parent.id),
                        tuple -> {
                            Long count = tuple.get(comment.count()); // value: comment count
                            return count == null ? 0 : Math.toIntExact(count);
                        })
                );
    }

    @Override
    public Map<Long, List<PostComment>> findTopNRepliesByParentIds(List<Long> parentIds, int limit) {
        QPostComment comment = QPostComment.postComment;
        QUser user = QUser.user;

        Map<Long, List<PostComment>> replyMap = new LinkedHashMap<>();

        for (Long parentId : parentIds) {
            List<PostComment> replies = queryFactory
                    .selectFrom(comment)
                    .join(comment.user, user).fetchJoin()
                    .where(
                            comment.valid.isTrue(),
                            comment.parent.id.eq(parentId)
                    )
                    .orderBy(comment.id.asc()) // 오래된 순
                    .limit(limit)
                    .fetch();

            replyMap.put(parentId, replies);
        }

        return replyMap;
    }

    @Override
    public List<PostComment> findParentCommentsByPost(Post post) {
        QPostComment comment = QPostComment.postComment;
        QUser user = QUser.user;

        return queryFactory
                .selectFrom(comment)
                .join(comment.user, user).fetchJoin()
                .where(
                        comment.valid.isTrue(),
                        comment.post.eq(post),
                        comment.parent.isNull()
                )
                .orderBy(comment.id.asc())
                .fetch();
    }

    @Override
    public List<PostComment> getRepliesByParentId(Long parentId, Long cursorId, int limit) {
        QPostComment comment = QPostComment.postComment;
        QUser user = QUser.user;

        BooleanExpression baseCondition = comment.valid.isTrue()
                .and(comment.parent.id.eq(parentId));

        if (cursorId != null) {
            baseCondition = baseCondition.and(comment.id.gt(cursorId));
        }

        return queryFactory
                .selectFrom(comment)
                .join(comment.user, user).fetchJoin()
                .where(baseCondition)
                .orderBy(comment.id.asc())
                .limit(limit)
                .fetch();
    }

    @Override
    public List<PostComment> getMyComments(User user, Long cursorId, int limit) {
        QPostComment comment = QPostComment.postComment;
        QPost post = QPost.post;

        return queryFactory
                .selectFrom(comment)
                .join(comment.post, post).fetchJoin()
                .where(
                        comment.user.eq(user),
                        comment.valid.isTrue(),
                        comment.parent.isNull(),
                        cursorId != null ? comment.id.lt(cursorId) : null
                )
                .orderBy(comment.id.desc())
                .limit(limit)
                .fetch();
    }
}
