package com.socialmedia.clover_network.repository;

import com.socialmedia.clover_network.entity.GroupRolePermission;
import com.socialmedia.clover_network.enumuration.GroupMemberRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface GroupRolePermissionRepository extends JpaRepository<GroupRolePermission, Long> {
    Optional<GroupRolePermission> findByGroupIdAndGroupRoleId(String groupId, GroupMemberRole roleId);
}
