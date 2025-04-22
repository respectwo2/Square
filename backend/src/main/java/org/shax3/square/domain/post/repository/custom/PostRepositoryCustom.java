package org.shax3.square.domain.post.repository.custom;

import org.shax3.square.domain.post.model.Post;
import org.shax3.square.domain.user.model.User;

import java.util.List;

public interface PostRepositoryCustom {
    List<Post> findPopularPosts(int count);

    List<Post> findByLatestCursor(Long cursorId, int limit);

    List<Post> findByLikesCursor(Integer cursorLikes, Long cursorId, int limit);

    List<Post> findMyPosts(User user, Long nextCursorId, int limit);

    List<Post> findMyLikedPosts(User user, Long nextCursorId, int limit);

    List<Post> findMyScrapPosts(User user, Long nextCursorId, int limit);
}
