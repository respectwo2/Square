package org.shax3.square.domain.scrap.repository;

import org.shax3.square.common.model.TargetType;
import org.shax3.square.domain.scrap.model.Scrap;
import org.shax3.square.domain.scrap.repository.custom.ScrapRepositoryCustom;
import org.shax3.square.domain.user.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ScrapRepository extends JpaRepository<Scrap, Long>, ScrapRepositoryCustom {

    void deleteByUserAndTargetIdAndTargetType(User user, Long targetId, TargetType targetType);

    boolean existsByUserAndTargetIdAndTargetType(User user, Long targetId, TargetType targetType);

    List<Scrap> findByUserAndTargetType(User user, TargetType targetType);
}
