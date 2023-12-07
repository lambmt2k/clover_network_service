package com.socialmedia.clover_network.repository;

import com.socialmedia.clover_network.entity.TokenItem;
import com.socialmedia.clover_network.entity.UserInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TokenItemRepository extends JpaRepository<TokenItem, String> {
    List<TokenItem> findByUserId(String userId);
    List<TokenItem> findByUserIdAndDelFlagFalseOrderByCreatedTimeDesc(String userId);
    Optional<TokenItem> findByTokenId(String tokenId);
}
