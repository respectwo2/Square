package org.shax3.square.domain.like.repository;

import org.shax3.square.common.model.TargetType;
import org.shax3.square.domain.like.model.Like;
import org.shax3.square.domain.like.repository.custom.LikeRepositoryCustom;
import org.shax3.square.domain.user.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LikeRepository extends JpaRepository<Like, Long>, LikeRepositoryCustom {

    List<Like> findByTargetIdAndTargetTypeAndUserIn(Long targetId, TargetType targetType, List<User> users);

    boolean existsByUserAndTargetTypeAndTargetIdAndLikeTrue(User user, TargetType targetType, Long targetId);

}
