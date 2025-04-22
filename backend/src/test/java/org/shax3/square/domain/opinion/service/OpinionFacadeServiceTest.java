package org.shax3.square.domain.opinion.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.shax3.square.common.model.TargetType;
import org.shax3.square.domain.debate.model.Debate;
import org.shax3.square.domain.like.service.LikeService;
import org.shax3.square.domain.opinion.dto.request.CreateOpinionCommentRequest;
import org.shax3.square.domain.opinion.dto.response.CreateOpinionCommentResponse;
import org.shax3.square.domain.opinion.dto.response.MyOpinionResponse;
import org.shax3.square.domain.opinion.dto.response.OpinionDetailsResponse;
import org.shax3.square.domain.opinion.model.Opinion;
import org.shax3.square.domain.opinion.model.OpinionComment;
import org.shax3.square.domain.s3.service.S3Service;
import org.shax3.square.domain.user.model.Type;
import org.shax3.square.domain.user.model.User;
import org.shax3.square.exception.CustomException;
import org.shax3.square.exception.ExceptionCode;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OpinionFacadeServiceTest {

    @Mock
    private OpinionService opinionService;
    @Mock
    private OpinionCommentService opinionCommentService;
    @Mock
    private S3Service s3Service;
    @Mock
    private LikeService likeService;
    @Mock
    private RedisTemplate<String, String> batchRedisTemplate;
    @Mock
    private SetOperations<String, String> setOperations;

    @InjectMocks
    private OpinionFacadeService opinionFacadeService;


    private User mockUser;
    private Opinion mockOpinion;
    private Debate mockDebate;

    @BeforeEach
    void setUp() {
        mockUser = User.builder()
                .nickname("TestUser")
                .type(Type.PNSB)
                .s3Key("test-key")
                .build();
        ReflectionTestUtils.setField(mockUser, "id", 1L);

        mockOpinion = Opinion.builder()
                .user(mockUser)
                .content("Sample Content")
                .build();
        ReflectionTestUtils.setField(mockOpinion, "id", 1L);
        ReflectionTestUtils.setField(mockOpinion, "likeCount", 10);
        ReflectionTestUtils.setField(mockOpinion, "createdAt", LocalDateTime.now());


        mockDebate = Debate.builder()
                .topic("Sample Debate")
                .build();
        ReflectionTestUtils.setField(mockDebate, "id", 1L);

    }

    @Test
    @DisplayName("답글 생성 성공 테스트")
    void createOpinionComment_success() {
        CreateOpinionCommentRequest request = new CreateOpinionCommentRequest(1L, "Sample Comment Content");
        OpinionComment newComment = OpinionComment.builder()
                .user(mockUser)
                .opinion(mockOpinion)
                .content("Sample Comment Content")
                .build();
        ReflectionTestUtils.setField(newComment, "id", 1L);

        when(opinionService.getOpinion(1L)).thenReturn(mockOpinion);
        when(opinionCommentService.createComment(any(), any(), any())).thenReturn(newComment);
        when(s3Service.generatePresignedGetUrl(any())).thenReturn("presigned-url");

        CreateOpinionCommentResponse response = opinionFacadeService.createOpinionComment(mockUser, request);

        assertThat(response).isNotNull();
        assertThat(response.commentId()).isEqualTo(1L);
        assertThat(response.profileUrl()).isEqualTo("presigned-url");

        verify(opinionService).getOpinion(1L);
        verify(opinionCommentService).createComment(any(), any(), any());
        verify(s3Service).generatePresignedGetUrl(any());
    }


    @Test
    @DisplayName("답글 생성 실패 테스트 - 의견이 존재하지 않을 경우")
    void createOpinionComment_opinionNotFound() {
        CreateOpinionCommentRequest request = new CreateOpinionCommentRequest(999L, "Sample Comment Content");

        when(opinionService.getOpinion(999L)).thenThrow(new CustomException(ExceptionCode.OPINION_NOT_FOUND));

        assertThatThrownBy(() -> opinionFacadeService.createOpinionComment(mockUser, request))
                .isInstanceOf(CustomException.class)
                .hasMessage(ExceptionCode.OPINION_NOT_FOUND.getMessage());

        verify(opinionCommentService, never()).createComment(any(), any(), any());
    }

    @Test
    @DisplayName("의견 상세 조회 성공 테스트")
    void getOpinionDetails_success() {
        // given
        OpinionComment comment = new OpinionComment(1L, mockOpinion, mockUser, "Nice!", 5, true);
        List<OpinionComment> mockOpinionComments = List.of(comment);


        when(opinionService.getOpinion(1L)).thenReturn(mockOpinion);
        when(opinionCommentService.getOpinionComments(1L)).thenReturn(mockOpinionComments);
        when(s3Service.generatePresignedGetUrl("test-key")).thenReturn("presigned-url");

        when(batchRedisTemplate.opsForSet()).thenReturn(setOperations);
        when(setOperations.members("like:batch")).thenReturn(Set.of());

        // opinion에 대해 유저가 좋아요 누른 상태
        when(likeService.getLikedTargetIds(mockUser, TargetType.OPINION, List.of(1L)))
                .thenReturn(Set.of(1L));

        // comment에 대해 좋아요 누른 상태
        when(likeService.getLikedTargetIds(mockUser, TargetType.OPINION_COMMENT, List.of(1L)))
                .thenReturn(Set.of(1L));

        OpinionDetailsResponse response = opinionFacadeService.getOpinionDetails(mockUser, 1L);

        assertThat(response).isNotNull();
        assertThat(response.opinionId()).isEqualTo(1L);
        assertThat(response.userType()).isEqualTo(Type.PNSB.name());
        assertThat(response.nickname()).isEqualTo("TestUser");
        assertThat(response.profileUrl()).isEqualTo("presigned-url");
        assertThat(response.isLiked()).isTrue();
        assertThat(response.comments()).hasSize(1);
        assertThat(response.comments().get(0).isLiked()).isTrue();
    }

    @Test
    @DisplayName("의견 상세 조회 실패 테스트 - 의견이 존재하지 않을 경우")
    void getOpinionDetails_opinionNotFound() {
        when(opinionService.getOpinion(1L)).thenThrow(new CustomException(ExceptionCode.OPINION_NOT_FOUND));

        assertThatThrownBy(() -> opinionFacadeService.getOpinionDetails(mockUser, 1L))
                .isInstanceOf(CustomException.class)
                .hasMessage(ExceptionCode.OPINION_NOT_FOUND.getMessage());

        verify(opinionCommentService, never()).getOpinionComments(any());
    }

    @Test
    @DisplayName("댓글이 없는 경우 의견 상세 조회 성공 테스트")
    void getOpinionDetails_noComments() {
        when(opinionService.getOpinion(1L)).thenReturn(mockOpinion);
        when(opinionCommentService.getOpinionComments(1L)).thenReturn(List.of());
        when(s3Service.generatePresignedGetUrl("test-key")).thenReturn("presigned-url");

        when(batchRedisTemplate.opsForSet()).thenReturn(setOperations);
        when(setOperations.members("like:batch")).thenReturn(Set.of());

        OpinionDetailsResponse response = opinionFacadeService.getOpinionDetails(mockUser, 1L);

        assertThat(response).isNotNull();
        assertThat(response.opinionId()).isEqualTo(1L);
        assertThat(response.comments()).isEmpty();
    }

    @DisplayName("내 의견 목록 조회 - 첫 페이지 (데이터 있음)")
    @Test
    void getMyOpinions_FirstPage_WithData() {
        // Given
        Long nextCursorId = null;
        int limit = 3;
        List<Opinion> opinions = createTestOpinions(5L, 4L, 3L);

        when(opinionService.getMyOpinions(mockUser, nextCursorId, limit)).thenReturn(opinions);
        when(likeService.getLikedTargetIds(eq(mockUser), eq(TargetType.OPINION), anyList()))
                .thenReturn(Set.of(4L));

        when(batchRedisTemplate.opsForSet()).thenReturn(setOperations);
        when(setOperations.members("like:batch")).thenReturn(Set.of());

        // When
        MyOpinionResponse response = opinionFacadeService.getMyOpinions(mockUser, nextCursorId, limit);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.opinions()).hasSize(3);
        assertThat(response.opinions().get(0).opinionId()).isEqualTo(5L);
        assertThat(response.opinions().get(1).opinionId()).isEqualTo(4L);
        assertThat(response.opinions().get(1).isLiked()).isTrue();
        assertThat(response.opinions().get(2).opinionId()).isEqualTo(3L);
        assertThat(response.nextCursorId()).isEqualTo(3L);
    }

    @DisplayName("내 의견 목록 조회 - 두 번째 페이지 (데이터 있음)")
    @Test
    void getMyOpinions_SecondPage_WithData() {
        Long nextCursorId = 3L;
        int limit = 3;
        List<Opinion> opinions = createTestOpinions(2L, 1L);

        when(opinionService.getMyOpinions(mockUser, nextCursorId, limit)).thenReturn(opinions);
        when(likeService.getLikedTargetIds(eq(mockUser), eq(TargetType.OPINION), anyList())).thenReturn(Set.of());

        when(batchRedisTemplate.opsForSet()).thenReturn(setOperations);
        when(setOperations.members("like:batch")).thenReturn(Set.of());

        MyOpinionResponse response = opinionFacadeService.getMyOpinions(mockUser, nextCursorId, limit);

        assertThat(response.opinions()).hasSize(2);
        assertThat(response.opinions().get(0).opinionId()).isEqualTo(2L);
        assertThat(response.opinions().get(1).opinionId()).isEqualTo(1L);
        assertThat(response.nextCursorId()).isEqualTo(1L);
        assertThat(response.opinions().stream().allMatch(o -> !o.isLiked())).isTrue();
    }

    @DisplayName("내 의견 목록 조회 - 마지막 페이지 (데이터 없음)")
    @Test
    void getMyOpinions_LastPage_NoMoreData() {
        Long nextCursorId = 1L;
        int limit = 3;

        when(opinionService.getMyOpinions(mockUser, nextCursorId, limit)).thenReturn(List.of());

        when(batchRedisTemplate.opsForSet()).thenReturn(setOperations);
        when(setOperations.members("like:batch")).thenReturn(Set.of());

        MyOpinionResponse response = opinionFacadeService.getMyOpinions(mockUser, nextCursorId, limit);

        assertThat(response.opinions()).isEmpty();
        assertThat(response.nextCursorId()).isNull();
    }

    @DisplayName("내 의견 목록 조회 - 데이터가 없는 경우")
    @Test
    void getMyOpinions_NoData() {
        Long nextCursorId = null;
        int limit = 3;

        when(opinionService.getMyOpinions(mockUser, nextCursorId, limit)).thenReturn(List.of());

        when(batchRedisTemplate.opsForSet()).thenReturn(setOperations);
        when(setOperations.members("like:batch")).thenReturn(Set.of());

        MyOpinionResponse response = opinionFacadeService.getMyOpinions(mockUser, nextCursorId, limit);

        assertThat(response.opinions()).isEmpty();
        assertThat(response.nextCursorId()).isNull();
    }

    @DisplayName("내 의견 목록 조회 - 정확히 limit 개수만 있는 경우")
    @Test
    void getMyOpinions_ExactlyLimitData() {
        Long nextCursorId = null;
        int limit = 3;
        List<Opinion> opinions = createTestOpinions(5L, 4L, 3L);

        when(opinionService.getMyOpinions(mockUser, nextCursorId, limit)).thenReturn(opinions);
        when(likeService.getLikedTargetIds(eq(mockUser), eq(TargetType.OPINION), anyList())).thenReturn(Set.of(3L));

        when(batchRedisTemplate.opsForSet()).thenReturn(setOperations);
        when(setOperations.members("like:batch")).thenReturn(Set.of());

        MyOpinionResponse response = opinionFacadeService.getMyOpinions(mockUser, nextCursorId, limit);

        assertThat(response.opinions()).hasSize(3);
        assertThat(response.opinions().get(2).isLiked()).isTrue(); // 3L
        assertThat(response.nextCursorId()).isEqualTo(3L);
    }

    // 테스트용 Opinion 목록 생성 헬퍼 메서드
    private List<Opinion> createTestOpinions(Long... ids) {
        List<Opinion> opinions = new ArrayList<>();

        for (Long id : ids) {
            Opinion opinion = Opinion.builder()
                    .user(mockUser)
                    .debate(mockDebate)
                    .content("테스트 내용 " + id)
                    .build();
            ReflectionTestUtils.setField(opinion, "id", id);
            ReflectionTestUtils.setField(opinion, "valid", true);
            opinions.add(opinion);
        }
        return opinions;
    }

}



