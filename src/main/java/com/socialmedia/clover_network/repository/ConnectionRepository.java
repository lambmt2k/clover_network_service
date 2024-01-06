package com.socialmedia.clover_network.repository;

import com.socialmedia.clover_network.entity.Connection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ConnectionRepository extends JpaRepository<Connection, Long> {
    Optional<Connection> findByUserIdAndUserIdConnectedAndConnectStatusTrue(String userId, String userIdConnected);

    Connection findByUserIdAndUserIdConnected(String userId, String userIdConnected);
    List<Connection> findByUserIdAndConnectStatusTrueAndDelFlagFalse(String userId);
    List<Connection> findByUserIdAndConnectStatusTrueAndDelFlagFalse(String userId, Pageable pageable);
    List<Connection> findByUserIdConnectedAndConnectStatusTrueAndDelFlagFalse(String userId);
    List<Connection> findByUserIdConnectedAndConnectStatusTrueAndDelFlagFalse(String userId, Pageable pageable);

    @Query(value = "SELECT c1.* FROM connection c1 " +
            "INNER JOIN connection c2 ON c1.user_id = c2.user_id_connected AND c1.user_id_connected = c2.user_id " +
            "WHERE c1.user_id = :user_id AND c1.connect_status = true AND c2.connect_status = true " +
            "ORDER BY c1.time_connect DESC",
            countQuery = "SELECT COUNT(*) FROM connection c1 " +
                    "INNER JOIN connection c2 ON c1.user_id = c2.user_id_connected AND c1.user_id_connected = c2.user_id " +
                    "WHERE c1.user_id = :user_id AND c1.connect_status = true AND c2.connect_status = true",
            nativeQuery = true)
    Page<Connection> findFriendsByUserId(@Param("user_id") String userId, Pageable pageable);
}
