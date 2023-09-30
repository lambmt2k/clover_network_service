package com.socialmedia.clover_network.repository;

import com.socialmedia.clover_network.entity.GroupMember;
import com.socialmedia.clover_network.enumuration.GroupMemberRole;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GroupMemberRepository extends JpaRepository<GroupMember, Long> {
    List<GroupMember> findByUserIdAndDelFlagFalse(String userId);
    List<GroupMember> findAllByGroupId(String groupId);
    Optional<GroupMember> findByUserIdAndGroupId(String userId, String groupId);
    Optional<GroupMember> findByGroupIdAndGroupRoleIdAndDelFlagFalse(String groupId, GroupMemberRole groupRoleId);

    @Query(value = "SELECT gm.* " +
            "FROM group_member gm " +
            "LEFT JOIN user_info u on gm.user_id = u.user_id " +
            "WHERE gm.group_id = ?1 " +
            "      AND gm.del_flag = false " +
            "      and gm.group_role_id = ?2 " +
            "      and u.status = 1", nativeQuery = true)
    Page<GroupMember> getActiveRoleOfGroupByGroupId(String groupId, int roleId, Pageable pageable);

}
