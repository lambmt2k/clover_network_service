package com.socialmedia.clover_network.repository;

import com.socialmedia.clover_network.entity.PostItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FeedRepository extends JpaRepository<PostItem, String> {
}
