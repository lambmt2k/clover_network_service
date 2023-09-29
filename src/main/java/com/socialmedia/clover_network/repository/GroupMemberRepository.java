package com.socialmedia.clover_network.repository;

import com.socialmedia.clover_network.entity.GroupMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GroupMemberRepository extends JpaRepository<GroupMember, Long> {
    List<GroupMember> findByUserIdAndDelFlagFalse(String userId);
    List<GroupMember> findAllByGroupId(String groupId);
    Optional<GroupMember> findByUserIdAndGroupId(String userId, String groupId);
}
