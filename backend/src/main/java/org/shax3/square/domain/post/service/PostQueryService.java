package org.shax3.square.domain.post.service;

import lombok.RequiredArgsConstructor;
import org.shax3.square.domain.post.model.Post;
import org.shax3.square.domain.post.repository.PostRepository;
import org.shax3.square.domain.user.model.User;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PostQueryService {

    private final PostRepository postRepository;

    public List<Post> getPopularPosts(int limit) {
        return postRepository.findPopularPosts(limit);
    }

    // 최신순: id 내림차순
    public List<Post> getPostsByLatestCursor(Long cursorId, int limit) {
        return postRepository.findByLatestCursor(cursorId, limit + 1);
    }

    // 좋아요순: likeCount 내림차순, id 내림차순
    public List<Post> getPostsByLikesCursor(Integer cursorLikes, Long cursorId, int limit) {
        return postRepository.findByLikesCursor(cursorLikes, cursorId, limit + 1);
    }

    public List<Post> getMyPosts(User user, Long nextCursorId, int limit) {
        return postRepository.findMyPosts(user, nextCursorId, limit + 1);
    }

    public List<Post> getMyLikedPosts(User user, Long nextCursorId, int limit) {
        return postRepository.findMyLikedPosts(user, nextCursorId, limit + 1);
    }

    public List<Post> getMyScrapPosts(User user, Long nextCursorId, int limit) {
        return postRepository.findMyScrapPosts(user, nextCursorId, limit);
    }
}
