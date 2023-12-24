package com.socialmedia.clover_network.repository;

import com.socialmedia.clover_network.entity.ReactionItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReactionItemRepository extends JpaRepository<ReactionItem, Long> {
    ReactionItem findByAuthorIdAndPostIdAndDelFlagFalse(String authorId, String postId);
    List<ReactionItem> findByPostIdAndDelFlagFalse(String postId);
}
