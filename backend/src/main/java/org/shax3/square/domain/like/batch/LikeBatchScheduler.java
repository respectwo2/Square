package org.shax3.square.domain.like.batch;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.shax3.square.common.model.TargetType;
import org.shax3.square.domain.like.service.LikeService;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class LikeBatchScheduler {

    private final RedisTemplate<String, Object> batchRedisTemplate;
    private final LikeService likeService;

    @Scheduled(fixedRate = 300_000)
    public void flushLikes() {
        Set<Object> entries = batchRedisTemplate.opsForSet().members("like:batch");

        if (entries == null || entries.isEmpty()) return;

        // String -> POST:1, List<Long> -> [101, 102], ex) POST:1:[101, 102])
        Map<String, List<Long>> grouped = new HashMap<>();

        for (Object entryObj : entries) {
            String entry = entryObj.toString(); // ex) POST:1:101
            String[] parts = entry.split(":");
            if (parts.length != 3) continue;

            String type = parts[0];
            Long targetId = Long.valueOf(parts[1]);
            Long userId = Long.valueOf(parts[2]);

            String key = type + ":" + targetId;
            grouped.computeIfAbsent(key, k -> new ArrayList<>()).add(userId); // key가 없으면 새로운 ArrayList 생성
        }

        for (Map.Entry<String, List<Long>> group : grouped.entrySet()) {
            String[] parts = group.getKey().split(":");
            TargetType targetType = TargetType.valueOf(parts[0]);
            Long targetId = Long.valueOf(parts[1]);
            List<Long> userIds = group.getValue();

            likeService.persistLikes(userIds, targetType, targetId);
        }

        batchRedisTemplate.delete("like:batch");
    }
}
