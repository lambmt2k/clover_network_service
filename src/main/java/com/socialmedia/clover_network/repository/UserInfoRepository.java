package com.socialmedia.clover_network.repository;

import com.socialmedia.clover_network.entity.UserInfo;
import com.socialmedia.clover_network.enumuration.UserStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserInfoRepository extends JpaRepository<UserInfo, String> {
    Optional<UserInfo> findByEmail(String email);

    Optional<UserInfo> findByUserId(String userId);
    Optional<UserInfo> findByUserIdAndStatus(String userId, UserStatus status);

    List<UserInfo> findByUserIdIn(List<String> userIds);

    @Query(value = "SELECT u FROM UserInfo u WHERE u.userRole = 1 AND u.userId NOT IN :excludedUserIds" +
            " AND u.status = 1 ORDER BY RAND()")
    List<UserInfo> findRandom10Users(@Param("excludedUserIds") List<String> excludedUserIds, Pageable pageable);

    List<UserInfo> findByStatus(UserStatus userStatus);

    @Query(value = "SELECT u.* FROM user_info u " +
            "WHERE LOWER(CONCAT(u.firstname, ' ' , u.lastname)) LIKE LOWER(CONCAT('%', :keyword, '%'))", nativeQuery = true)
    List<UserInfo> findByFirstNameOrLastNameContainingIgnoreCase(String keyword);
}
