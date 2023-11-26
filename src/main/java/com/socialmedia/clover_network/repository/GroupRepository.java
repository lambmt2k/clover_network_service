package com.socialmedia.clover_network.repository;

import com.socialmedia.clover_network.entity.GroupEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GroupRepository extends JpaRepository<GroupEntity, Long> {
    List<GroupEntity> findByGroupOwnerId(String groupOwnerId);

    Optional<GroupEntity> findByGroupOwnerIdAndGroupType(String groupOwnerId, GroupEntity.GroupType groupType);

    Optional<GroupEntity> findByGroupIdAndGroupType(String groupId, GroupEntity.GroupType groupType);

    List<GroupEntity> findByGroupTypeAndGroupIdIn(GroupEntity.GroupType groupType, List<String> groupIds);

    Optional<GroupEntity> findByGroupId(String groupId);

    List<GroupEntity> findByDelFlagFalseAndGroupNameContaining(String keyword);
}
