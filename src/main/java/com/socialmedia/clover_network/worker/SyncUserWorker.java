package com.socialmedia.clover_network.worker;

import com.socialmedia.clover_network.entity.GroupMember;
import com.socialmedia.clover_network.entity.UserInfo;
import com.socialmedia.clover_network.enumuration.UserStatus;
import com.socialmedia.clover_network.repository.GroupMemberRepository;
import com.socialmedia.clover_network.repository.UserInfoRepository;
import com.socialmedia.clover_network.service.GroupService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@EnableScheduling
@Configuration
public class SyncUserWorker {
    private final Logger logger = LoggerFactory.getLogger(SyncUserWorker.class);

    @Autowired
    UserInfoRepository userInfoRepository;
    @Autowired
    GroupMemberRepository groupMemberRepository;
    @Autowired
    GroupService groupService;

    @Scheduled(cron = "00 00 03 * * ?", zone = "Asia/Ho_Chi_Minh")
    public void syncAllUserToAllGroupSystem() {
        logger.info("[syncAllUserToAllGroupSystem] Start add all user to Clover Network Community with groupId = 1191451836392214528");
        List<UserInfo> allUserActiveInfo = userInfoRepository.findByStatus(UserStatus.ACTIVE);
        String groupId = "1191451836392214528";

        ExecutorService executor = Executors.newFixedThreadPool(20);
        for (UserInfo user : allUserActiveInfo) {
            executor.submit(() -> {
                if (user != null) {
                    String userId = user.getUserId();
                    Optional<GroupMember> groupMemberOpt = groupMemberRepository.findByUserIdAndGroupId(userId, groupId);
                    if (groupMemberOpt.isEmpty()) {
                        groupService.addMemberList(groupId, userId,false);
                    }
                }
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            });
        }
        try {
            executor.shutdown();
            while (!executor.awaitTermination(24L, TimeUnit.SECONDS)) {
                System.out.println("Not yet. Still waiting for termination");
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
