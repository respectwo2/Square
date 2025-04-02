package org.shax3.square.domain.post.repository;

import java.util.List;

import org.shax3.square.domain.post.model.Post;

public interface PostRepositoryCustom {
	List<Post> findPopularPosts(int count);
	List<Post> findByLatestCursor(Long cursorId, int limit);
	List<Post> findByLikesCursor(Integer cursorLikes, int limit);
}
