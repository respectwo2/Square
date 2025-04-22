package org.shax3.square.domain.opinion.service;

import lombok.RequiredArgsConstructor;
import org.shax3.square.common.model.TargetType;
import org.shax3.square.domain.like.service.LikeService;
import org.shax3.square.domain.notification.event.DebateCommentCreatedEvent;
import org.shax3.square.domain.opinion.dto.MyOpinionDto;
import org.shax3.square.domain.opinion.dto.OpinionDto;
import org.shax3.square.domain.opinion.dto.request.CreateOpinionCommentRequest;
import org.shax3.square.domain.opinion.dto.response.CommentResponse;
import org.shax3.square.domain.opinion.dto.response.CreateOpinionCommentResponse;
import org.shax3.square.domain.opinion.dto.response.MyOpinionResponse;
import org.shax3.square.domain.opinion.dto.response.OpinionDetailsResponse;
import org.shax3.square.domain.opinion.model.Opinion;
import org.shax3.square.domain.opinion.model.OpinionComment;
import org.shax3.square.domain.s3.service.S3Service;
import org.shax3.square.domain.user.model.User;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@RequiredArgsConstructor
@Service
public class OpinionFacadeService {
    private final OpinionService opinionService;
    private final OpinionCommentService opinionCommentService;
    private final S3Service s3Service;
    private final LikeService likeService;
    private final RedisTemplate<String, Object> batchRedisTemplate;
    private final ApplicationEventPublisher eventPublisher;

    /* 답글을 생성하기 위한 메서드
    - 답글에 opinionId가 필요하기 때문에
      이 opinionService와 opinionCommentService 두개를 참조해야하는 함
     */
    @Transactional
    public CreateOpinionCommentResponse createOpinionComment(User user, CreateOpinionCommentRequest request) {
        Opinion opinion = opinionService.getOpinion(request.opinionId());
        String profileUrl = s3Service.generatePresignedGetUrl(user.getS3Key());

        OpinionComment newComment = opinionCommentService.createComment(request, opinion, user);

        // 답글 알림
        User opinionAuthor = opinion.getUser();
        if (!opinionAuthor.getId().equals(user.getId())) {
            eventPublisher.publishEvent(new DebateCommentCreatedEvent(
                    opinionAuthor,
                    newComment.getContent(),
                    opinion.getId()
            ));
        }

        return CreateOpinionCommentResponse.of(newComment, profileUrl);
    }

    /*
        의견정보와 그 의견에 달린 답글 모두 조회해야함
     */
    @Transactional(readOnly = true)
    public OpinionDetailsResponse getOpinionDetails(User user, Long opinionId) {
        Opinion opinion = opinionService.getOpinion(opinionId);
        List<OpinionComment> comments = opinionCommentService.getOpinionComments(opinionId);

        // 좋아요 여부 판단
        boolean isLiked = likeService.getLikedTargetIds(user, TargetType.OPINION, List.of(opinionId))
                .contains(opinionId);

        Set<Object> entries = batchRedisTemplate.opsForSet().members("like:batch");
        int likeCount = opinion.getLikeCount() + likeService.getLikeCountInRedis(entries, opinionId, TargetType.OPINION);

        List<Long> commentIds = comments.stream()
                .map(OpinionComment::getId)
                .toList();

        Set<Long> likedCommentIds = likeService.getLikedTargetIds(user, TargetType.OPINION_COMMENT, commentIds);

        List<CommentResponse> commentResponses = comments.stream()
                .map(comment -> CommentResponse.of(
                        comment,
                        s3Service.generatePresignedGetUrl(comment.getUser().getS3Key()),
                        likedCommentIds.contains(comment.getId()),
                        comment.getLikeCount() + likeService.getLikeCountInRedis(entries, comment.getId(), TargetType.OPINION_COMMENT)
                ))
                .toList();

        String profileUrl = s3Service.generatePresignedGetUrl(opinion.getUser().getS3Key());

        return OpinionDetailsResponse.of(opinion, commentResponses, profileUrl, isLiked, likeCount);
    }

    @Transactional(readOnly = true)
    public MyOpinionResponse getMyOpinions(User user, Long nextCursorId, int limit) {
        List<Opinion> opinions = opinionService.getMyOpinions(user, nextCursorId, limit);

        List<Long> opinionIds = opinions.stream()
                .map(Opinion::getId)
                .toList();

        Set<Long> likedOpinionIds = likeService.getLikedTargetIds(user, TargetType.OPINION, opinionIds);

        Long newNextCursorId = opinions.isEmpty() ? null : opinions.get(opinions.size() - 1).getId();

        Set<Object> entries = batchRedisTemplate.opsForSet().members("like:batch");

        List<MyOpinionDto> opinionDtos = opinions.stream()
                .map(opinion -> {
                    boolean isLiked = likedOpinionIds.contains(opinion.getId());
                    int likeCount = opinion.getLikeCount() + likeService.getLikeCountInRedis(entries, opinion.getId(), TargetType.OPINION);
                    return MyOpinionDto.from(opinion, isLiked, likeCount);
                })
                .toList();

        return MyOpinionResponse.of(opinionDtos, newNextCursorId);
    }

    @Transactional(readOnly = true)
    public List<OpinionDto> getOpinionsBySort(
            User user,
            Long debateId,
            boolean isLeft,
            String sort,
            Long nextCursorId,
            Integer nextCursorLikes,
            Integer nextCursorComments,
            int limit
    ) {
        List<Opinion> opinions = switch (sort) {
            case "likes" -> opinionService.findOpinionsByLikes(debateId, isLeft, nextCursorId, nextCursorLikes, limit);
            case "comments" ->
                    opinionService.findOpinionsByComments(debateId, isLeft, nextCursorId, nextCursorComments, limit);
            default -> opinionService.findOpinionsByLatest(debateId, isLeft, nextCursorId, limit);
        };

        List<Long> opinionIds = opinions.stream().map(Opinion::getId).toList();
        Set<Long> likedOpinionIds = likeService.getLikedTargetIds(user, TargetType.OPINION, opinionIds);

        Set<Object> entries = batchRedisTemplate.opsForSet().members("like:batch");

        return opinions.stream()
                .map(opinion -> OpinionDto.of(
                        opinion,
                        likedOpinionIds.contains(opinion.getId()),
                        opinion.getCommentCount(),
                        opinion.getUser(),
                        s3Service.generatePresignedGetUrl(opinion.getUser().getS3Key()),
                        opinion.getLikeCount() + likeService.getLikeCountInRedis(entries, opinion.getId(), TargetType.OPINION)
                ))
                .toList();
    }
}

