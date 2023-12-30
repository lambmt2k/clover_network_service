package com.socialmedia.clover_network.repository;

import com.socialmedia.clover_network.entity.OTPEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OTPRepository extends JpaRepository<OTPEntity, Long> {
    Optional<OTPEntity> findTopByEmailOrderByCreatedTimeDesc(String email);
}
