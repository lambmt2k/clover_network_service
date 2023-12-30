package com.socialmedia.clover_network.repository;

import com.socialmedia.clover_network.entity.PostItem;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FeedRepository extends JpaRepository<PostItem, String> {
    List<PostItem> findAllByPrivacyGroupIdInAndDelFlagFalseAndLastActiveIsNotNullOrderByLastActiveDesc(List<String> privacyGroupIds, Pageable pageable);
    List<PostItem> findByPrivacyGroupIdAndDelFlagFalse(String privacyGroupId);

    List<PostItem> findByDelFlagFalseAndContentContainingIgnoreCase(String keyword);

    PostItem findByPostIdAndDelFlagFalse(String postId);

    List<PostItem> findAllByPostIdInAndDelFlagFalseAndLastActiveIsNotNullOrderByLastActiveDesc(List<String> postId, Pageable pageable);
    List<PostItem> findAllByPostIdInAndPrivacyGroupIdInAndDelFlagFalseAndLastActiveIsNotNullOrderByLastActiveDesc(List<String> postId, List<String> groupIds, Pageable pageable);
}
