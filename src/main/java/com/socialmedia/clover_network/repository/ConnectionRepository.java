package com.socialmedia.clover_network.repository;

import com.socialmedia.clover_network.entity.Connection;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ConnectionRepository extends JpaRepository<Connection, Long> {
    Optional<Connection> findByUserIdAndUserIdConnectedAndConnectStatusTrue(String userId, String userIdConnected);

    Connection findByUserIdAndUserIdConnected(String userId, String userIdConnected);
    List<Connection> findByUserIdAndConnectStatusTrue(String userId);
    List<Connection> findByUserIdAndConnectStatusTrue(String userId, Pageable pageable);
    List<Connection> findByUserIdConnectedAndConnectStatusTrue(String userId);
    List<Connection> findByUserIdConnectedAndConnectStatusTrue(String userId, Pageable pageable);
}
