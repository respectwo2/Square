package org.shax3.square.domain.opinion.service;

import lombok.RequiredArgsConstructor;
import org.shax3.square.domain.opinion.dto.request.CreateOpinionCommentRequest;
import org.shax3.square.domain.opinion.dto.request.UpdateOpinionRequest;
import org.shax3.square.domain.opinion.model.Opinion;
import org.shax3.square.domain.opinion.model.OpinionComment;
import org.shax3.square.domain.opinion.repository.OpinionCommentRepository;
import org.shax3.square.domain.user.model.User;
import org.shax3.square.exception.CustomException;
import org.shax3.square.exception.ExceptionCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.shax3.square.exception.ExceptionCode.OPINION_COMMENT_NOT_FOUND;

@Service
@RequiredArgsConstructor
public class OpinionCommentService {
    private final OpinionCommentRepository opinionCommentRepository;

    public List<OpinionComment> getOpinionComments(Long opinionId) {
        return opinionCommentRepository.findByOpinionId(opinionId);
    }

    public OpinionComment createComment(
            CreateOpinionCommentRequest request,
            Opinion opinion,
            User user
    ) {

        opinion.increaseCommentCount();
        return opinionCommentRepository.save(request.to(opinion, user));
    }


    @Transactional
    public void deleteOpinionComment(User user, Long commentId) {
        OpinionComment comment = getOpinionComment(commentId);

        if (!comment.getUser().getId().equals(user.getId())) {
            throw new CustomException(ExceptionCode.NOT_AUTHOR);
        }

        if (comment.isValid()) {
            comment.softDelete();
            comment.getOpinion().decreaseCommentCount();
        }
    }

    @Transactional
    public void updateOpinionComment(User user, UpdateOpinionRequest request, Long commentId) {
        OpinionComment comment = getOpinionComment(commentId);

        if (!comment.getUser().getId().equals(user.getId())) {
            throw new CustomException(ExceptionCode.NOT_AUTHOR);
        }

        comment.updateContent(request.content());
    }

    public OpinionComment getOpinionComment(Long opinionCommentId) {
        return opinionCommentRepository.findById(opinionCommentId)
                .orElseThrow(() -> new CustomException(OPINION_COMMENT_NOT_FOUND));
    }

    public void validateExists(Long opinionCommentId) {
        if (!opinionCommentRepository.existsById(opinionCommentId)) {
            throw new CustomException(OPINION_COMMENT_NOT_FOUND);
        }
    }

    public void increaseLikeCount(Long targetId, int countDiff) {
        OpinionComment opinionComment = getOpinionComment(targetId);
        opinionComment.increaseLikeCount(countDiff);
    }

}
