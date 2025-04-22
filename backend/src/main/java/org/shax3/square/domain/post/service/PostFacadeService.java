package org.shax3.square.domain.post.service;

import lombok.RequiredArgsConstructor;
import org.shax3.square.common.model.TargetType;
import org.shax3.square.domain.like.service.LikeService;
import org.shax3.square.domain.post.dto.*;
import org.shax3.square.domain.post.dto.response.MyPostResponse;
import org.shax3.square.domain.post.dto.response.PostDetailResponse;
import org.shax3.square.domain.post.dto.response.PostListResponse;
import org.shax3.square.domain.post.model.Post;
import org.shax3.square.domain.post.model.PostComment;
import org.shax3.square.domain.s3.service.S3Service;
import org.shax3.square.domain.scrap.service.ScrapService;
import org.shax3.square.domain.user.model.User;
import org.shax3.square.exception.CustomException;
import org.shax3.square.exception.ExceptionCode;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.shax3.square.common.util.CursorUtil.getNextCursor;
import static org.shax3.square.common.util.CursorUtil.hasNext;

@Service
@RequiredArgsConstructor
public class PostFacadeService {

    private final PostQueryService postQueryService;
    private final PostCommentService commentService;
    private final LikeService likeService;
    private final S3Service s3Service;
    private final PostService postService;
    private final ScrapService scrapService;
    private final RedisTemplate<String, Object> batchRedisTemplate;

    /**
     * 게시글 목록 조회
     *
     * @param user
     * @param sort
     * @param nextCursorId
     * @param nextCursorLikes
     * @param limit
     * @return
     */
    @Transactional(readOnly = true)
    public PostListResponse getPostList(
            User user,
            String sort,
            Long nextCursorId,
            Integer nextCursorLikes,
            int limit
    ) {

        // 성향테스트 해야만 게시글 조회 가능
        if (user.getType() == null) {
            throw new CustomException(ExceptionCode.USER_TYPE_NOT_FOUND);
        }

        // 상단 인기 게시글 조회 (3개)
        List<PopularPostDto> popularDtos = toPopularDtos(postQueryService.getPopularPosts(3));

        // 정렬 기준에 따라 게시글 조회
        boolean isSortByLikes = "likes".equalsIgnoreCase(sort);
        List<Post> fetchedPosts = isSortByLikes
                ? postQueryService.getPostsByLikesCursor(nextCursorLikes, nextCursorId, limit)
                : postQueryService.getPostsByLatestCursor(nextCursorId, limit);

        boolean hasNext = hasNext(fetchedPosts, limit);
        List<Post> posts = hasNext ? fetchedPosts.subList(0, limit) : fetchedPosts;

        List<PostSummaryDto> postDtos = toPostDtos(posts, user);

        return PostListResponse.of(
                user.getType().name(),
                popularDtos,
                postDtos,
                getNextCursor(posts, hasNext, Post::getId),
                isSortByLikes ? getNextCursor(posts, hasNext, Post::getLikeCount) : null
        );
    }

    /**
     * 내가 쓴 게시글 조회
     *
     * @param user
     * @param nextCursorId
     * @param limit
     * @return
     */
    @Transactional(readOnly = true)
    public MyPostResponse getMyPostList(User user, Long nextCursorId, int limit) {

        List<Post> fetchedPosts = postQueryService.getMyPosts(user, nextCursorId, limit);

        boolean hasNext = hasNext(fetchedPosts, limit);
        List<Post> posts = hasNext ? fetchedPosts.subList(0, limit) : fetchedPosts;

        List<PostSummaryDto> postDtos = toPostDtos(posts, user);

        return new MyPostResponse(postDtos, getNextCursor(posts, hasNext, Post::getId));
    }

    /**
     * 내가 좋아요한 게시글 조회
     *
     * @param user
     * @param nextCursorId
     * @param limit
     * @return
     */
    @Transactional(readOnly = true)
    public MyPostResponse getMyLikedPostList(User user, Long nextCursorId, int limit) {

        List<Post> fetchedPosts = postQueryService.getMyLikedPosts(user, nextCursorId, limit);

        boolean hasNext = hasNext(fetchedPosts, limit);
        List<Post> posts = hasNext ? fetchedPosts.subList(0, limit) : fetchedPosts;

        List<PostSummaryDto> postDtos = toPostDtos(posts, user);

        return new MyPostResponse(postDtos, getNextCursor(posts, hasNext, Post::getId));
    }

    /**
     * 내가 스크랩한 게시글 조회
     *
     * @param user
     * @param nextCursorId
     * @param limit
     * @return
     */
    @Transactional(readOnly = true)
    public MyPostResponse getMyScrapPostList(User user, Long nextCursorId, int limit) {
        List<Post> fetchedPosts = postQueryService.getMyScrapPosts(user, nextCursorId, limit + 1);

        boolean hasNext = hasNext(fetchedPosts, limit);
        List<Post> posts = hasNext ? fetchedPosts.subList(0, limit) : fetchedPosts;

        List<PostSummaryDto> postDtos = toPostDtos(posts, user);

        return new MyPostResponse(postDtos, getNextCursor(posts, hasNext, Post::getId));
    }


    private List<PopularPostDto> toPopularDtos(List<Post> popularPosts) {
        List<Long> popularPostIds = popularPosts.stream().map(Post::getId).toList();
        Set<Object> entries = batchRedisTemplate.opsForSet().members("like:batch");

        // key: postId, value: commentCount
        Map<Long, Integer> commentCounts = commentService.getCommentCounts(popularPostIds);

        return popularPosts.stream()
                .map(post -> PopularPostDto.from(
                        post,
                        commentCounts.getOrDefault(post.getId(), 0),
                        post.getLikeCount() + likeService.getLikeCountInRedis(entries, post.getId(), TargetType.POST)
                ))
                .toList();
    }

    private List<PostSummaryDto> toPostDtos(List<Post> posts, User user) {
        List<Long> postIds = posts.stream().map(Post::getId).toList();
        Set<Long> likedPostIds = likeService.getLikedTargetIds(user, TargetType.POST, postIds);

        Set<Object> entries = batchRedisTemplate.opsForSet().members("like:batch");

        // key: postId, value: commentCount
        Map<Long, Integer> commentCounts = commentService.getCommentCounts(postIds);

        return posts.stream()
                .map(post -> PostSummaryDto.from(
                        post,
                        s3Service.generatePresignedGetUrl(post.getUser().getS3Key()),
                        likedPostIds.contains(post.getId()),
                        commentCounts.getOrDefault(post.getId(), 0),
                        post.getLikeCount() + likeService.getLikeCountInRedis(entries, post.getId(), TargetType.POST)
                ))
                .toList();
    }

    /**
     * 게시글 상세 조회
     *
     * @param user
     * @param postId
     * @return
     */
    @Transactional(readOnly = true)
    public PostDetailResponse getPostDetail(User user, Long postId) {
        Post post = postService.getPost(postId);

        // 이미지 DTO 변환
        List<PostImageDto> imageDtos = post.getPostImages().stream()
                .map(image -> new PostImageDto(
                        s3Service.generatePresignedGetUrl(image.getS3Key()),
                        image.getS3Key()))
                .toList();

        // 좋아요/스크랩 여부
        boolean isLiked = likeService.isTargetLiked(user, TargetType.POST, postId);
        boolean isScraped = scrapService.isTargetScraped(user, postId, TargetType.POST);

        Set<Object> entries = batchRedisTemplate.opsForSet().members("like:batch");
        int likeCount = post.getLikeCount() + likeService.getLikeCountInRedis(entries, postId, TargetType.POST);

        // 댓글 목록 (parent만)
        List<PostComment> comments = commentService.getParentComments(post);
        List<Long> commentIds = extractCommentIds(comments);

        // 대댓글 개수 및 3개씩 조회
        Map<Long, Integer> replyCounts = commentService.getReplyCounts(commentIds);
        Map<Long, List<PostComment>> replyMap = commentService.getFirstNRepliesByCommentIds(commentIds, 3);

        // 댓글, 대댓글 좋아요 정보
        Set<Long> likedCommentIds = likeService.getLikedTargetIds(user, TargetType.POST_COMMENT, commentIds);
        Set<Long> likedReplyIds = likeService.getLikedTargetIds(user, TargetType.POST_COMMENT, extractReplyIds(replyMap));


        List<CommentDto> commentDtos = comments.stream()
                .map(comment -> CommentDto.from(
                        comment,
                        s3Service.generatePresignedGetUrl(comment.getUser().getS3Key()),
                        likedCommentIds.contains(comment.getId()),
                        replyCounts.getOrDefault(comment.getId(), 0),
                        replyMap.getOrDefault(comment.getId(), List.of()).stream()
                                .map(reply -> ReplyDto.from(
                                        reply,
                                        s3Service.generatePresignedGetUrl(reply.getUser().getS3Key()),
                                        likedReplyIds.contains(reply.getId()),
                                        reply.getLikeCount() + likeService.getLikeCountInRedis(entries, reply.getId(), TargetType.POST_COMMENT)
                                ))
                                .toList(),
                        comment.getLikeCount() + likeService.getLikeCountInRedis(entries, comment.getId(), TargetType.POST_COMMENT)
                ))
                .toList();

        return PostDetailResponse.from(
                post,
                imageDtos,
                s3Service.generatePresignedGetUrl(post.getUser().getS3Key()),
                isLiked,
                isScraped,
                commentDtos,
                commentDtos.size(),
                likeCount
        );
    }


    private List<Long> extractCommentIds(List<PostComment> comments) {
        return comments.stream().map(PostComment::getId).toList();
    }

    private List<Long> extractReplyIds(Map<Long, List<PostComment>> replyMap) {
        return replyMap.values().stream().flatMap(List::stream).map(PostComment::getId).toList();
    }
}
