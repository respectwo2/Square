package org.shax3.square.domain.user.repository;

import org.shax3.square.domain.user.model.User;
import org.shax3.square.domain.user.model.UserDevice;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserDeviceRepository extends JpaRepository<UserDevice, Long> {
    Optional<UserDevice> findByDeviceId(String deviceId);

    List<UserDevice> findAllByUser(User user);

    void deleteByDeviceId(String deviceId);

}
