package com.socialmedia.clover_network.repository;

import com.socialmedia.clover_network.entity.CommentItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CommentItemRepository extends JpaRepository<CommentItem, Long> {
}
