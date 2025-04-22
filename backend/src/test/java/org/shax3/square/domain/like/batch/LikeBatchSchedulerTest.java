package org.shax3.square.domain.like.batch;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.shax3.square.common.model.TargetType;
import org.shax3.square.domain.like.service.LikeService;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SetOperations;

import java.util.List;
import java.util.Set;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LikeBatchSchedulerTest {

    @Mock
    private RedisTemplate<String, Object> batchRedisTemplate;
    @Mock
    private SetOperations<String, Object> setOperations;
    @Mock
    private LikeService likeService;

    @InjectMocks
    private LikeBatchScheduler likeBatchScheduler;

    @Test
    @DisplayName("flushLikes - 정상적으로 그룹핑 및 호출")
    void flushLikes_success() {
        // given
        Set<Object> redisData = Set.of(
                "OPINION:1:100",
                "OPINION:1:101",
                "PROPOSAL:2:102"
        );

        when(batchRedisTemplate.opsForSet()).thenReturn(setOperations);
        when(setOperations.members("like:batch")).thenReturn(redisData);

        // when
        likeBatchScheduler.flushLikes();

        // then
        verify(likeService).persistLikes(
                argThat(list -> list.containsAll(List.of(100L, 101L)) && list.size() == 2),
                eq(TargetType.OPINION),
                eq(1L)
        );
        verify(likeService).persistLikes(List.of(102L), TargetType.PROPOSAL, 2L);
        verify(batchRedisTemplate).delete("like:batch");
    }

    @Test
    @DisplayName("flushLikes - Redis 데이터 없음 → 아무것도 하지 않음")
    void flushLikes_empty() {
        when(batchRedisTemplate.opsForSet()).thenReturn(setOperations);
        when(setOperations.members("like:batch")).thenReturn(Set.of());

        likeBatchScheduler.flushLikes();

        verify(likeService, never()).persistLikes(any(), any(), anyLong());
        verify(batchRedisTemplate, never()).delete(anyString());
    }
}