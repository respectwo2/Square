package org.shax3.square.domain.like.handler;

import lombok.RequiredArgsConstructor;
import org.shax3.square.common.model.TargetType;
import org.shax3.square.domain.opinion.service.OpinionCommentService;
import org.shax3.square.domain.opinion.service.OpinionService;
import org.shax3.square.domain.post.service.PostCommentService;
import org.shax3.square.domain.post.service.PostService;
import org.shax3.square.domain.proposal.service.ProposalService;
import org.shax3.square.exception.CustomException;
import org.shax3.square.exception.ExceptionCode;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class LikeTargetHandler {

    private final OpinionService opinionService;
    private final ProposalService proposalService;
    private final OpinionCommentService opinionCommentService;
    private final PostService postService;
    private final PostCommentService postCommentService;

    public void validateTargetExists(Long targetId, TargetType targetType) {
        switch (targetType) {
            case OPINION -> opinionService.validateExists(targetId);
            case OPINION_COMMENT -> opinionCommentService.validateExists(targetId);
            case PROPOSAL -> proposalService.validateExists(targetId);
            case POST -> postService.validateExists(targetId);
            case POST_COMMENT -> postCommentService.validateExists(targetId);
            default -> throw new CustomException(ExceptionCode.INVALID_TARGET_TYPE);
        }
    }

    public void updateTargetLikeCount(TargetType targetType, Long targetId, int diff) {
        switch (targetType) {
            case OPINION -> opinionService.increaseLikeCount(targetId, diff);
            case PROPOSAL -> proposalService.increaseLikeCount(targetId, diff);
            case OPINION_COMMENT -> opinionCommentService.increaseLikeCount(targetId, diff);
            case POST -> postService.increaseLikeCount(targetId, diff);
            case POST_COMMENT -> postCommentService.increaseLikeCount(targetId, diff);
            default -> throw new CustomException(ExceptionCode.INVALID_TARGET_TYPE);
        }
    }
}
