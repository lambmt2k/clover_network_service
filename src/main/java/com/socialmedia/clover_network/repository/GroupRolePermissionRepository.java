package com.socialmedia.clover_network.repository;

import com.socialmedia.clover_network.entity.GroupRolePermission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GroupRolePermissionRepository extends JpaRepository<GroupRolePermission, Long> {
}
