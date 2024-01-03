package com.socialmedia.clover_network.service;

import com.socialmedia.clover_network.dto.NotificationItem;
import com.socialmedia.clover_network.entity.NotificationEntity;
import com.socialmedia.clover_network.entity.UserDeviceToken;
import com.socialmedia.clover_network.mapper.NotificationMapper;
import com.socialmedia.clover_network.repository.NotificationRepository;
import com.socialmedia.clover_network.repository.UserDeviceTokenRepository;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class NotificationService {
    private final Logger logger = LoggerFactory.getLogger(NotificationService.class);

    @Autowired
    NotificationRepository notificationRepository;
    @Autowired
    FirebaseService firebaseService;
    @Autowired
    private UserDeviceTokenRepository userDeviceTokenRepository;

    public Long createNotificationItem(NotificationItem notificationItem) {
        NotificationEntity notificationEntity = NotificationMapper.INSTANCE.toEntity(notificationItem);
        NotificationEntity data = notificationRepository.save(notificationEntity);
        return data.getNotificationId();
    }

    public void broadcastNotification(Long notificationId, List<String> listUserIds, Map<String, String> customData) {
        logger.info("[broadcastNotification] Start broadcast notification");
        //TODO: push external channel (firebase, ...) : Firebase Cloud Messaging (FCM)

        //Distinct list userId
        Set<String> distinctMemberList = new HashSet<>(listUserIds);
        if (CollectionUtils.isNotEmpty(distinctMemberList)) {
            ExecutorService executor = Executors.newFixedThreadPool(20);
            for (String userId : distinctMemberList) {
                executor.submit(() -> {
                    logger.info("[broadcastNotification] to userId: " + userId);
                    NotificationItem pushItem = NotificationMapper.INSTANCE.toDTO(notificationRepository.findById(notificationId).orElse(null));
                    List<UserDeviceToken> userDeviceTokens = userDeviceTokenRepository.findByUserId(userId);
                    if (pushItem != null && !userDeviceTokens.isEmpty()) {
                        List<String> tokenList = userDeviceTokens.stream().map(UserDeviceToken::getToken).collect(Collectors.toList());
                        firebaseService.sendToDeviceByPushItem(pushItem, tokenList, userId, customData);
                    }
                });
                logger.info("[broadcastNotification] Finish broadcast notification userID: " + userId);
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
}
