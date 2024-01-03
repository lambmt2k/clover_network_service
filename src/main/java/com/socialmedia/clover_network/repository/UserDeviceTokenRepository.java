package com.socialmedia.clover_network.repository;

import com.socialmedia.clover_network.entity.UserDeviceToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserDeviceTokenRepository extends JpaRepository<UserDeviceToken, Long> {
    UserDeviceToken findByUserIdAndToken(String userId, String token);
    List<UserDeviceToken> findByUserId(String userId);
}
