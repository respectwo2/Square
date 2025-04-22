package org.shax3.square.domain.opinion.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.shax3.square.domain.opinion.dto.request.UpdateOpinionRequest;
import org.shax3.square.domain.opinion.model.Opinion;
import org.shax3.square.domain.opinion.model.OpinionComment;
import org.shax3.square.domain.opinion.repository.OpinionCommentRepository;
import org.shax3.square.domain.user.model.Type;
import org.shax3.square.domain.user.model.User;
import org.shax3.square.exception.CustomException;
import org.shax3.square.exception.ExceptionCode;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OpinionCommentServiceTest {

    @Mock
    private OpinionCommentRepository opinionCommentRepository;

    @InjectMocks
    private OpinionCommentService opinionCommentService;

    private User mockUser;
    private OpinionComment mockComment;
    private Opinion mockOpinion;

    @BeforeEach
    void setUp() {
        mockUser = User.builder()
                .nickname("TestUser")
                .type(Type.PNSB)
                .s3Key("test-key")
                .build();
        ReflectionTestUtils.setField(mockUser, "id", 1L);

        mockOpinion = Opinion.builder()
                .content("Sample Opinion Content")
                .build();
        ReflectionTestUtils.setField(mockOpinion, "id", 1L);

        mockComment = OpinionComment.builder()
                .user(mockUser)
                .opinion(mockOpinion)
                .content("Sample Comment")
                .build();
        ReflectionTestUtils.setField(mockComment, "id", 1L);


    }

    @Test
    @DisplayName("댓글을 성공적으로 반환하는지 테스트")
    void getOpinionComments_success() {
        // Given
        OpinionComment mockComment = new OpinionComment(1L, null, mockUser, "Nice!", 5, true);
        List<OpinionComment> mockComments = List.of(mockComment);

        when(opinionCommentRepository.findByOpinionId(1L)).thenReturn(mockComments);
        // when(s3Service.generatePresignedGetUrl("test-key")).thenReturn("presigned-url");

        // When
        List<OpinionComment> comments = opinionCommentService.getOpinionComments(1L);

        // Then
        assertThat(comments).isNotNull().hasSize(1);
        assertThat(comments.get(0).getContent()).isEqualTo("Nice!");
        assertThat(comments.get(0).getLikeCount()).isEqualTo(5);
        assertThat(comments.get(0).getUser().getNickname()).isEqualTo("TestUser");
        // assertThat(responses.get(0).profileUrl()).isEqualTo("presigned-url");
    }

    @Test
    @DisplayName("댓글이 없는 경우 빈 리스트 반환 테스트")
    void getOpinionComments_emptyList() {
        // Given
        when(opinionCommentRepository.findByOpinionId(1L)).thenReturn(List.of());

        // When
        List<OpinionComment> comments = opinionCommentService.getOpinionComments(1L);

        // Then
        assertThat(comments).isNotNull().isEmpty();
    }


    @Test
    @DisplayName("답글 삭제 성공 테스트 - Soft Delete")
    void deleteOpinionComment_success() {
        // Given
        when(opinionCommentRepository.findById(1L)).thenReturn(Optional.of(mockComment));

        // When
        opinionCommentService.deleteOpinionComment(mockUser, 1L);

        // Then
        assertThat(mockComment.isValid()).isFalse();
        verify(opinionCommentRepository, times(1)).findById(1L);
        verify(opinionCommentRepository, never()).delete(any(OpinionComment.class));
    }

    @Test
    @DisplayName("답글 삭제 실패 테스트 - 댓글이 존재하지 않을 경우")
    void deleteOpinionComment_notFound() {
        // Given
        when(opinionCommentRepository.findById(999L)).thenReturn(Optional.empty());

        // When / Then
        assertThatThrownBy(() -> opinionCommentService.deleteOpinionComment(mockUser, 999L))
                .isInstanceOf(CustomException.class)
                .hasMessage(ExceptionCode.OPINION_COMMENT_NOT_FOUND.getMessage());

        verify(opinionCommentRepository, times(1)).findById(999L);
    }

    @Test
    @DisplayName("답글 삭제 실패 테스트 - 작성자가 아닌 경우")
    void deleteOpinionComment_notAuthor() {
        // Given
        User anotherUser = User.builder()
                .nickname("AnotherUser")
                .type(Type.PNSB)
                .s3Key("another-key")
                .build();
        ReflectionTestUtils.setField(anotherUser, "id", 2L);

        when(opinionCommentRepository.findById(1L)).thenReturn(Optional.of(mockComment));

        // When / Then
        assertThatThrownBy(() -> opinionCommentService.deleteOpinionComment(anotherUser, 1L))
                .isInstanceOf(CustomException.class)
                .hasMessage(ExceptionCode.NOT_AUTHOR.getMessage());

        verify(opinionCommentRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("답글 업데이트 성공 테스트")
    void updateOpinionComment_success() {
        // Given
        UpdateOpinionRequest updateRequest = new UpdateOpinionRequest("Updated Content");

        when(opinionCommentRepository.findById(1L)).thenReturn(Optional.of(mockComment));

        // When
        opinionCommentService.updateOpinionComment(mockUser, updateRequest, 1L);

        // Then
        assertThat(mockComment.getContent()).isEqualTo("Updated Content");
        verify(opinionCommentRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("답글 업데이트 실패 테스트 - 댓글이 존재하지 않을 경우")
    void updateOpinionComment_notFound() {
        // Given
        UpdateOpinionRequest updateRequest = new UpdateOpinionRequest("Updated Content");

        when(opinionCommentRepository.findById(999L)).thenReturn(Optional.empty());

        // When / Then
        assertThatThrownBy(() -> opinionCommentService.updateOpinionComment(mockUser, updateRequest, 999L))
                .isInstanceOf(CustomException.class)
                .hasMessage(ExceptionCode.OPINION_COMMENT_NOT_FOUND.getMessage());

        verify(opinionCommentRepository, times(1)).findById(999L); // findById 호출 검증
    }

    @Test
    @DisplayName("답글 업데이트 실패 테스트 - 작성자가 아닌 경우")
    void updateOpinionComment_notAuthor() {
        // Given
        User anotherUser = User.builder()
                .nickname("AnotherUser")
                .type(Type.PNSB)
                .s3Key("another-key")
                .build();
        ReflectionTestUtils.setField(anotherUser, "id", 2L);

        UpdateOpinionRequest updateRequest = new UpdateOpinionRequest("Updated Content");

        when(opinionCommentRepository.findById(1L)).thenReturn(Optional.of(mockComment));
        // When / Then
        assertThatThrownBy(() -> opinionCommentService.updateOpinionComment(anotherUser, updateRequest, 1L))
                .isInstanceOf(CustomException.class)
                .hasMessage(ExceptionCode.NOT_AUTHOR.getMessage());

        verify(opinionCommentRepository, times(1)).findById(1L); // findById 호출 검증
    }

    @Test
    @DisplayName("답글이 존재하는 경우 true 반환")
    void isOpinionCommentExists_whenPresent() {
        // given
        Long opinionCommentId = 3L;
        when(opinionCommentRepository.existsById(opinionCommentId)).thenReturn(true);

        // when & then
        assertThatCode(() -> opinionCommentService.validateExists(opinionCommentId))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("답글이 존재하지 않는 경우 false 반환")
    void isOpinionCommentExists_whenNotPresent() {
        // given
        Long opinionCommentId = 4L;
        when(opinionCommentRepository.existsById(opinionCommentId)).thenReturn(false);

        // when & then
        assertThatThrownBy(() -> opinionCommentService.validateExists(opinionCommentId))
                .isInstanceOf(CustomException.class)
                .hasMessage(ExceptionCode.OPINION_COMMENT_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("increaseLikeCount - likeCount 증가 로직이 잘 호출되는지 확인")
    void increaseLikeCount_success() {
        // given
        OpinionComment opinionComment = mock(OpinionComment.class);
        when(opinionCommentRepository.findById(10L)).thenReturn(Optional.of(opinionComment));

        // when
        opinionCommentService.increaseLikeCount(10L, 3);

        // then
        verify(opinionComment).increaseLikeCount(3);
    }
}
