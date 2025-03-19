package org.shax3.square.domain.user.repository;

import org.shax3.square.domain.user.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserJpaRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);

}
