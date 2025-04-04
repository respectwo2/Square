package org.shax3.square.domain.post.repository.custom;

import java.util.List;
import java.util.Map;

import org.shax3.square.domain.post.model.Post;
import org.shax3.square.domain.post.model.PostComment;

public interface PostCommentRepositoryCustom {
	Map<Long, Integer> countCommentsByPostIds(List<Long> postIds);

	Map<Long, Integer> countRepliesByParentIds(List<Long> parentCommentIds);

	Map<Long, List<PostComment>> findTopNRepliesByParentIds(List<Long> parentCommentIds, int limit);

	List<PostComment> findParentCommentsByPost(Post post);

	List<PostComment> getRepliesByParentId(Long parentId, Long cursorId, int limit);
}
