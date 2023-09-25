package com.socialmedia.clover_network.repository;

import com.socialmedia.clover_network.entity.GroupEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GroupRepository extends JpaRepository<GroupEntity, Long> {
    List<GroupEntity> findByGroupOwnerId(String groupOwnerId);
}
