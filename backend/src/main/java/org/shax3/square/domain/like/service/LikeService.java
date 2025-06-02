package org.shax3.square.domain.like.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
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
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LikeService {

    private final LikeRepository likeRepository;
    private final RedisTemplate<String, Object> batchRedisTemplate;
    private final UserService userService;
    private final LikeTargetHandler likeTargetHandler;

    /**
     * Redis에 좋아요 저장
     *
     * @param user
     * @param likeRequest
     */
    public void like(User user, LikeRequest likeRequest) {

        Long targetId = likeRequest.targetId();
        TargetType targetType = likeRequest.targetType();

        likeTargetHandler.validateTargetExists(targetId, targetType); // 좋아요를 누를 타겟이 있는지 확인

        toggleLikeInRedis(user, targetId, targetType); // Redis에 좋아요 저장
    }


    /**
     * Key:   like:batch
     * Value: TargetType:targetId:userId
     * Type:  Set
     */
    public void toggleLikeInRedis(User user, Long targetId, TargetType targetType) {

        String lockKey = String.format("like:rate_limit:%d:%s:%d", user.getId(), targetType.name(), targetId);
        Boolean isAllowed = batchRedisTemplate.opsForValue().setIfAbsent(lockKey, "1", 2, TimeUnit.SECONDS);

        if (!Boolean.TRUE.equals(isAllowed)) {
            throw new CustomException(ExceptionCode.TOO_MANY_REQUEST);
        }

        String key = "like:batch";
        String value = targetType.name() + ":" + targetId + ":" + user.getId();

        Boolean isMember = batchRedisTemplate.opsForSet().isMember(key, value); // Redis에 존재하는지 확인
        if (Boolean.TRUE.equals(isMember)) {
            batchRedisTemplate.opsForSet().remove(key, value);
        } else {
            batchRedisTemplate.opsForSet().add(key, value);
        }
    }

    /**
     * Redis에 저장된 좋아요를 DB에 저장
     *
     * @param userIds
     * @param targetType
     * @param targetId
     */
    @Transactional
    public void persistLikes(List<Long> userIds, TargetType targetType, Long targetId) {

        List<User> users = userService.findAllById(userIds);
        if (users.isEmpty()) return;

        List<Like> existsLikes = likeRepository.findByTargetIdAndTargetTypeAndUserIn(targetId, targetType, users);

        Set<Long> existsUserIds = existsLikes.stream()
                .map(like -> like.getUser().getId())
                .collect(Collectors.toSet());

        long toggledUp = existsLikes.stream().filter(like -> !like.isLike()).count();
        long toggledDown = existsLikes.stream().filter(Like::isLike).count();

        // 기존 좋아요는 toggle
        existsLikes.forEach(Like::toggleLike);

        // 새로운 좋아요는 생성
        List<Like> newLikes = users.stream()
                .filter(user -> !existsUserIds.contains(user.getId()))
                .map(user -> Like.builder()
                        .user(user)
                        .targetId(targetId)
                        .targetType(targetType)
                        .build())
                .toList();

        likeRepository.saveAll(newLikes);

        int likeDiff = (int) (newLikes.size() + toggledUp - toggledDown);
        likeTargetHandler.updateTargetLikeCount(targetType, targetId, likeDiff);
    }

    // 좋아요 되어있는 타겟 ID들을 가져오는 메서드
    public Set<Long> getLikedTargetIds(User user, TargetType targetType, List<Long> targetIds) {
        Set<Long> likedInDb = likeRepository.findLikedTargetIds(user, targetType, targetIds);
        Set<Long> likedInRedis = getLikedTargetIdsFromRedis(user, targetType, targetIds);

        likedInDb.addAll(likedInRedis);
        return likedInDb;
    }

    // Redis에 저장된 타겟 ID들을 가져오는 메서드
    private Set<Long> getLikedTargetIdsFromRedis(User user, TargetType targetType, List<Long> targetIds) {
        Set<Object> entries = batchRedisTemplate.opsForSet().members("like:batch");
        if (entries == null) return Set.of();

        return entries.stream()
                .map(Object::toString) // POST:123:1
                .filter(e -> {
                    String[] parts = e.split(":");
                    return parts.length == 3
                            && parts[0].equals(targetType.name())
                            && parts[2].equals(user.getId().toString())
                            && targetIds.contains(Long.valueOf(parts[1]));
                })
                .map(e -> Long.valueOf(e.split(":")[1]))
                .collect(Collectors.toSet());
    }

    // 특정 타겟에 대해 좋아요를 눌렀는지 확인하는 메서드
    public boolean isTargetLiked(User user, TargetType targetType, Long targetId) {
        return likeRepository.existsByUserAndTargetTypeAndTargetIdAndLikeTrue(user, targetType, targetId);
    }

    public int getLikeCountInRedis(Set<Object> entries, Long targetId, TargetType targetType) {
        if (entries == null) return 0;

        return (int) entries.stream()
                .map(Object::toString) // POST:123:1
                .filter(e -> {
                    String[] parts = e.split(":");
                    return parts.length == 3
                            && parts[0].equals(targetType.name())
                            && Long.valueOf(parts[1]).equals(targetId);
                })
                .count();
    }

}
