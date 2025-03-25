package org.shax3.square.domain.opinion.service;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.shax3.square.domain.opinion.dto.request.CreateOpinionCommentRequest;
import org.shax3.square.domain.opinion.dto.request.UpdateOpinionRequest;
import org.shax3.square.domain.opinion.dto.response.CommentResponse;
import org.shax3.square.domain.opinion.dto.response.CreateOpinionCommentResponse;
import org.shax3.square.domain.opinion.model.Opinion;
import org.shax3.square.domain.opinion.model.OpinionComment;
import org.shax3.square.domain.opinion.repository.OpinionCommentRepository;
import org.shax3.square.domain.s3.service.S3Service;
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
    private final S3Service s3Service;

    @Transactional(readOnly = true)
    public List<CommentResponse> getOpinionComments(User user, Long opinionId) {
        List<OpinionComment> comments = opinionCommentRepository.findByOpinionIdAndValidTrue(opinionId);

        return comments.stream()
                .map(comment -> CommentResponse.of(
                        comment,
                        s3Service.generatePresignedGetUrl(comment.getUser().getS3Key())
                ))
                .toList();
    }

    public OpinionComment createComment(
            CreateOpinionCommentRequest request,
            Opinion opinion,
            User user
    ) {
        return opinionCommentRepository.save(request.to(opinion, user));
    }


    @Transactional
    public void deleteOpinionComment(User user, Long commentId) {
        OpinionComment comment = opinionCommentRepository.findById(commentId)
                .orElseThrow(() -> new CustomException(OPINION_COMMENT_NOT_FOUND));

        if (!comment.getUser().getId().equals(user.getId())) {
            throw new CustomException(ExceptionCode.NOT_AUTHOR);
        }

        comment.softDelete();
    }

    @Transactional
    public void updateOpinionComment(User user, UpdateOpinionRequest request, Long commentId) {
        OpinionComment comment = opinionCommentRepository.findById(commentId)
                .orElseThrow(() -> new CustomException(OPINION_COMMENT_NOT_FOUND));

        if (!comment.getUser().getId().equals(user.getId())) {
            throw new CustomException(ExceptionCode.NOT_AUTHOR);
        }

        comment.updateContent(request.content());
    }
}
