package org.shax3.square.domain.like.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.shax3.square.common.model.TargetType;
import org.shax3.square.domain.like.dto.LikeRequest;
import org.shax3.square.domain.like.handler.LikeTargetHandler;
import org.shax3.square.domain.like.model.Like;
import org.shax3.square.domain.like.repository.LikeRepository;
import org.shax3.square.domain.user.model.User;
import org.shax3.square.domain.user.service.UserService;
import org.shax3.square.exception.CustomException;
import org.shax3.square.exception.ExceptionCode;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;
import static org.shax3.square.exception.ExceptionCode.OPINION_NOT_FOUND;

@ExtendWith(MockitoExtension.class)
class LikeServiceTest {

    @Mock
    private LikeRepository likeRepository;
    @Mock
    private RedisTemplate<String, Object> batchRedisTemplate;
    @Mock
    private SetOperations<String, Object> setOperations;
    @Mock
    private UserService userService;
    @Mock
    private LikeTargetHandler likeTargetHandler;

    @InjectMocks
    private LikeService likeService;

    private User user1;
    private User user2;
    private User user3;

    @BeforeEach
    void setUp() {
        user1 = User.builder()
                .nickname("TestUser")
                .build();
        ReflectionTestUtils.setField(user1, "id", 1L);

        user2 = User.builder()
                .nickname("TestUser2")
                .build();
        ReflectionTestUtils.setField(user2, "id", 2L);

        user3 = User.builder()
                .nickname("TestUser3")
                .build();
        ReflectionTestUtils.setField(user3, "id", 3L);
    }

    @Test
    @DisplayName("Redis에 좋아요 추가 - 처음 누를 때")
    void like_addToRedis() {
        // given
        Long targetId = 10L;
        TargetType targetType = TargetType.OPINION;
        LikeRequest request = new LikeRequest(targetId, targetType);

        doNothing().when(likeTargetHandler).validateTargetExists(targetId, targetType);
        when(batchRedisTemplate.opsForSet()).thenReturn(setOperations);
        when(setOperations.isMember("like:batch", "OPINION:10:1")).thenReturn(false);

        // when
        likeService.like(user1, request);

        // then
        verify(setOperations).add("like:batch", "OPINION:10:1");
    }

    @Test
    @DisplayName("Redis에서 좋아요 제거 - 이미 존재할 때")
    void like_removeFromRedis() {
        // given
        Long targetId = 10L;
        TargetType targetType = TargetType.OPINION;
        LikeRequest request = new LikeRequest(targetId, targetType);

        doNothing().when(likeTargetHandler).validateTargetExists(targetId, targetType);
        when(batchRedisTemplate.opsForSet()).thenReturn(setOperations);
        when(setOperations.isMember("like:batch", "OPINION:10:1")).thenReturn(true);

        // when
        likeService.like(user1, request);

        // then
        verify(setOperations).remove("like:batch", "OPINION:10:1");
    }

    @Test
    @DisplayName("존재하지 않는 대상에 좋아요 시도 → 예외 발생")
    void like_invalidTarget() {
        // given
        Long targetId = 10L;
        TargetType targetType = TargetType.OPINION;
        LikeRequest request = new LikeRequest(targetId, targetType);

        doThrow(new CustomException(OPINION_NOT_FOUND))
                .when(likeTargetHandler).validateTargetExists(targetId, targetType);

        // when & then
        assertThatThrownBy(() -> likeService.like(user1, request))
                .isInstanceOf(CustomException.class)
                .hasMessage(ExceptionCode.OPINION_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("persistLikes - 기존 좋아요 2개(toggled), 신규 1개(saveAll)")
    void persistLikes_toggleAndSaveNew() {
        // Given
        Long targetId = 40L;
        TargetType targetType = TargetType.OPINION;

        List<Long> userIds = List.of(1L, 2L, 3L);
        List<User> users = List.of(user1, user2, user3);

        Like like1 = Like.builder()
                .user(user1)
                .targetId(targetId)
                .targetType(targetType)
                .build();
        ReflectionTestUtils.setField(like1, "like", false); // false → true

        Like like2 = Like.builder()
                .user(user2)
                .targetId(targetId)
                .targetType(targetType)
                .build();
        ReflectionTestUtils.setField(like2, "like", true); // true → false

        when(userService.findAllById(userIds)).thenReturn(users);
        when(likeRepository.findByTargetIdAndTargetTypeAndUserIn(targetId, targetType, users))
                .thenReturn(List.of(like1, like2));

        // When
        likeService.persistLikes(userIds, targetType, targetId);


        // Then
        assertThat(like1.isLike()).isTrue();
        assertThat(like2.isLike()).isFalse();

        // user3만 신규 Like
        verify(likeRepository).saveAll(argThat(likes -> {
            List<Like> likeList = (List<Like>) likes;
            return likeList.size() == 1 &&
                    likeList.get(0).getUser().getId().equals(3L);
        }));
        verify(likeTargetHandler).updateTargetLikeCount(targetType, targetId, 1);
    }


    @Test
    @DisplayName("persistLikes - 유저 조회 결과 없음 (빈 리스트)")
    void persistLikes_noUsers() {
        // Given
        Long targetId = 99L;
        TargetType targetType = TargetType.PROPOSAL;
        List<Long> userIds = List.of(100L, 101L);

        when(userService.findAllById(userIds)).thenReturn(List.of()); // 유저 없음

        // When
        likeService.persistLikes(userIds, targetType, targetId);

        // Then
        verifyNoInteractions(likeRepository);
    }
}