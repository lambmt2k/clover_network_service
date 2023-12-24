package com.socialmedia.clover_network.repository;

import com.socialmedia.clover_network.entity.FeedUserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FeedUserRepository extends JpaRepository<FeedUserEntity, String> {
}
