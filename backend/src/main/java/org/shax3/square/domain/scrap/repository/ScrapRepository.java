package org.shax3.square.domain.scrap.repository;

import org.shax3.square.domain.scrap.model.Scrap;
import org.shax3.square.domain.scrap.model.TargetType;
import org.shax3.square.domain.user.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ScrapRepository extends JpaRepository<Scrap, Long> {

    void deleteByUserAndTargetIdAndTargetType(User user, Long targetId, TargetType targetType);

    Optional<Scrap> findByUserAndTargetIdAndTargetType(User user, Long targetId, TargetType targetType);

    List<Scrap> findByUserAndTargetType(User user, TargetType targetType);
}
