package com.socialmedia.clover_network.repository;

import com.socialmedia.clover_network.entity.FeedGroupEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FeedGroupRepository extends JpaRepository<FeedGroupEntity, String> {
}
