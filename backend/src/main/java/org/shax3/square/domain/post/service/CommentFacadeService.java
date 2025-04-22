package org.shax3.square.domain.post.service;

import lombok.RequiredArgsConstructor;
import org.shax3.square.common.model.TargetType;
import org.shax3.square.domain.like.service.LikeService;
import org.shax3.square.domain.post.dto.MyCommentDto;
import org.shax3.square.domain.post.dto.ReplyDto;
import org.shax3.square.domain.post.dto.response.MyCommentListResponse;
import org.shax3.square.domain.post.dto.response.RepliesResponse;
import org.shax3.square.domain.post.model.PostComment;
import org.shax3.square.domain.s3.service.S3Service;
import org.shax3.square.domain.user.model.User;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

import static org.shax3.square.common.util.CursorUtil.getNextCursor;
import static org.shax3.square.common.util.CursorUtil.hasNext;

@Service
@RequiredArgsConstructor
public class CommentFacadeService {

    private final PostCommentService commentService;
    private final LikeService likeService;
    private final S3Service s3Service;
    private final PostService postService;
    private final RedisTemplate<String, Object> batchRedisTemplate;

    /**
     * 대댓글 더보기 조회
     *
     * @param user
     * @param commentId
     * @param cursorId
     * @param limit
     * @return
     */
    @Transactional(readOnly = true)
    public RepliesResponse getReplies(User user, Long commentId, Long cursorId, int limit) {
        List<PostComment> fetchedReplies = commentService.getReplies(commentId, cursorId, limit);

        boolean hasNext = hasNext(fetchedReplies, limit);
        List<PostComment> replies = hasNext ? fetchedReplies.subList(0, limit) : fetchedReplies;

        // 좋아요 여부
        List<Long> replyIds = replies.stream().map(PostComment::getId).toList();
        Set<Long> likedReplyIds = likeService.getLikedTargetIds(user, TargetType.POST_COMMENT, replyIds);

        Set<Object> entries = batchRedisTemplate.opsForSet().members("like:batch");

        List<ReplyDto> replyDtos = replies.stream()
                .map(reply -> ReplyDto.from(
                        reply,
                        s3Service.generatePresignedGetUrl(reply.getUser().getS3Key()),
                        likedReplyIds.contains(reply.getId()),
                        reply.getLikeCount() + likeService.getLikeCountInRedis(entries, reply.getId(), TargetType.POST_COMMENT)
                ))
                .toList();

        Long nextCursorId = hasNext && !replies.isEmpty()
                ? replies.get(replies.size() - 1).getId()
                : null;

        return new RepliesResponse(replyDtos, getNextCursor(replies, hasNext, PostComment::getId));
    }

    @Transactional(readOnly = true)
    public MyCommentListResponse getMyComments(User user, Long nextCursorId, int limit) {
        List<PostComment> fetchedComments = commentService.getMyComments(user, nextCursorId, limit);

        boolean hasNext = hasNext(fetchedComments, limit);
        List<PostComment> comments = hasNext ? fetchedComments.subList(0, limit) : fetchedComments;

        // 좋아요 여부
        List<Long> commentIds = comments.stream().map(PostComment::getId).toList();
        Set<Long> likedIds = likeService.getLikedTargetIds(user, TargetType.POST_COMMENT, commentIds);

        Set<Object> entries = batchRedisTemplate.opsForSet().members("like:batch");

        List<MyCommentDto> dtos = comments.stream()
                .map(comment -> MyCommentDto.from(
                        comment,
                        likedIds.contains(comment.getId()),
                        comment.getLikeCount() + likeService.getLikeCountInRedis(entries, comment.getId(), TargetType.POST_COMMENT)
                ))
                .toList();

        Long nextCursor = getNextCursor(comments, hasNext, PostComment::getId);
        return new MyCommentListResponse(dtos, nextCursor);
    }
}
