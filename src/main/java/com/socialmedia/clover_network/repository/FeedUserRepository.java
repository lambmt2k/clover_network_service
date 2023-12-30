package com.socialmedia.clover_network.repository;

import com.socialmedia.clover_network.entity.FeedUserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FeedUserRepository extends JpaRepository<FeedUserEntity, String> {
    List<FeedUserEntity> findByValueContaining(String postId);
}
