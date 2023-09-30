package com.socialmedia.clover_network.repository;

import com.socialmedia.clover_network.entity.UserInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserInfoRepository extends JpaRepository<UserInfo, Long> {
    Optional<UserInfo> findByEmail(String email);
    Optional<UserInfo> findByUserId(String userId);
    List<UserInfo> findByUserIdIn(List<String> userIds);
}
