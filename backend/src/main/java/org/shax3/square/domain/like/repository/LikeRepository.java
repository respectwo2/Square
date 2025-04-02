package org.shax3.square.domain.like.repository;

import java.util.List;
import java.util.Optional;

import org.shax3.square.common.model.TargetType;
import org.shax3.square.domain.like.model.Like;
import org.shax3.square.domain.user.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LikeRepository extends JpaRepository<Like, Long>, LikeRepositoryCustom {

	List<Like> findByTargetIdAndTargetTypeAndUserIn(Long targetId, TargetType targetType, List<User> users);
}
