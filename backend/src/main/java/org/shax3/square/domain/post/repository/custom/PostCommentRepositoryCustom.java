package org.shax3.square.domain.post.repository.custom;

import org.shax3.square.domain.post.model.Post;
import org.shax3.square.domain.post.model.PostComment;
import org.shax3.square.domain.user.model.User;

import java.util.List;
import java.util.Map;

public interface PostCommentRepositoryCustom {
    Map<Long, Integer> countCommentsByPostIds(List<Long> postIds);

    Map<Long, Integer> countRepliesByParentIds(List<Long> parentCommentIds);

    Map<Long, List<PostComment>> findTopNRepliesByParentIds(List<Long> parentCommentIds, int limit);

    List<PostComment> findParentCommentsByPost(Post post);

    List<PostComment> getRepliesByParentId(Long parentId, Long cursorId, int limit);

    List<PostComment> getMyComments(User user, Long cursorId, int limit);
}
