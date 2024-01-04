package com.socialmedia.clover_network.service;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.*;
import com.google.firebase.cloud.StorageClient;
import com.google.firebase.messaging.*;
import com.socialmedia.clover_network.dto.FirebaseTokenUserItem;
import com.socialmedia.clover_network.dto.NotificationItem;
import com.socialmedia.clover_network.entity.UserDeviceToken;
import com.socialmedia.clover_network.enumuration.ImageType;
import com.socialmedia.clover_network.repository.UserDeviceTokenRepository;
import com.socialmedia.clover_network.util.GenIDUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

@Service
public class FirebaseService {
    @Value("${app_setting.firebase.bucket_name}")
    private String bucketName;

    private final Logger logger = LoggerFactory.getLogger(FirebaseService.class);

    @Autowired
    GenIDUtil genIDUtil;
    @Autowired
    UserDeviceTokenRepository userDeviceTokenRepository;

    private Storage storage;
    @EventListener
    public void init(ApplicationReadyEvent event) {
        try {
            ClassPathResource serviceAccount = new ClassPathResource("serviceAccountKey.json");
            storage = StorageOptions.newBuilder().
                    setCredentials(GoogleCredentials.fromStream(serviceAccount.getInputStream())).
                    setProjectId("clover-network-afd47").build().getService();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public String uploadImage(MultipartFile file, ImageType imageType) throws IOException {
        String fileName = generateUniqueFileName(file.getOriginalFilename());
        String prefixPath = "images/";
        switch (imageType) {
            case NONE: {
                break;
            }
            case USER_AVATAR: {
                prefixPath = prefixPath + "user_avatar/";
                break;
            }
            case USER_BANNER: {
                prefixPath = prefixPath + "user_banner/";
                break;
            }
            case FEED_IMAGES: {
                prefixPath = prefixPath + "feed_images/";
                break;
            }
            case GROUP_BANNER: {
                prefixPath = prefixPath + "group_banner/";
                break;
            }
            default: break;

        }
        BlobId blobId = BlobId.of(bucketName,  prefixPath + fileName);
        BlobInfo blobInfo = BlobInfo.newBuilder(blobId)
                .setContentType(file.getContentType())
                .build();
        storage.create(blobInfo, file.getInputStream());
        String imagePath = prefixPath + fileName;
        return imagePath;
    }

    public String getImagePublicUrl(String imagePath) {
        if (imagePath != null) {
            return StorageClient.getInstance().bucket().get(imagePath).signUrl(1, TimeUnit.HOURS).toString();
        }
        return null;
    }

    private String generateUniqueFileName(String originalFileName) {
        String extension = getFileExtension(originalFileName);
        return genIDUtil.genId() + "." + extension;
    }

    private String getFileExtension(String fileName) {
        int lastDotIndex = fileName.lastIndexOf('.');
        return (lastDotIndex == -1) ? "" : fileName.substring(lastDotIndex + 1);
    }

    private String getStorageUrl(String fileName) {
        //"https://storage.googleapis.com/"
        return bucketName + "/images/" + fileName;
    }

    public boolean isImage(MultipartFile file) {
        // Get the file name
        String fileName = file.getOriginalFilename();

        // Check if the file has a valid image extension (you can extend this list)
        return fileName != null && (fileName.endsWith(".jpg") || fileName.endsWith(".jpeg") || fileName.endsWith(".png"));
    }

    public void sendToDeviceByPushItem(NotificationItem notificationItem, List<String> tokenList, String userId, Map<String, String> customData) {
        Aps aps = Aps.builder().setContentAvailable(true).build();
        ApnsConfig apns = ApnsConfig.builder()
                .putHeader("apns-priority", "5")
                .setAps(aps)
                .build();
        AndroidConfig androidConfig = AndroidConfig.builder().setPriority(AndroidConfig.Priority.HIGH).build();
        String body, title;

        title = customData.getOrDefault("Title", null);
        switch (notificationItem.getTemplateId()) {
            case CONNECTION: {
                if (!customData.get("isConnectTogether").equals("true")) {
                    body = notificationItem.getUsername()
                            + " connected to you. Would you like to connect with "
                            + notificationItem.getUsername();
                } else {
                    body = notificationItem.getUsername()
                            + "has connected back to you. Now everyone can post on each other's wall.";
                }
                break;
            }
            default: {
                body = notificationItem.getMessage();
                break;
            }
        }
        tokenList.forEach(token -> {
            Message message = Message.builder()
                    .setToken(token)
                    .setNotification(Notification.builder().setTitle("Clover").setBody(body).build())
                    .putData("templateId", notificationItem.getTemplateId().name())
                    .putData("fromUserId", notificationItem.getFromUserId())
                    .putData("objectId", notificationItem.getObjectId())
                    .putData("fromGroupId", notificationItem.getFromGroupId())
                    .putData("groupName", notificationItem.getGroupName())
                    .putData("username", notificationItem.getUsername())
                    .putData("message", notificationItem.getMessage())
                    .putData("notificationId", notificationItem.getNotificationId().toString())
                    .putData("customData", customData.toString())
                    .setAndroidConfig(androidConfig)
                    .setApnsConfig(apns)
                    .build();
            try {
                String response = FirebaseMessaging.getInstance().send(message);
                logger.info("[sendToDevice] Success send notification firebase to device " + message + " | response: " + response);
            } catch (FirebaseMessagingException e) {
                logger.error("Fail to send firebase notification", e);
                removeDeviceToken(FirebaseTokenUserItem.builder().userId(userId).token(token).build());
                logger.info("Remove FCM token of user : " + userId + " | deviceToken: " + token);
            }
        });

    }

    public void pushDeviceToken(FirebaseTokenUserItem item) {
        logger.info("[pushDeviceToken] of user : " + item.getUserId() + " | deviceToken: " + item.getToken());
        UserDeviceToken existedDeviceToken = userDeviceTokenRepository.findByUserIdAndToken(item.getUserId(), item.getToken());
        if (Objects.isNull(existedDeviceToken)) {
            UserDeviceToken newUserDeviceToken = new UserDeviceToken();
            newUserDeviceToken.setUserId(item.getUserId());
            newUserDeviceToken.setToken(item.getToken());
            userDeviceTokenRepository.save(newUserDeviceToken);
        }
    }

    public void removeDeviceToken(FirebaseTokenUserItem item) {
        logger.info("[removeDeviceToken] of user : " + item.getUserId() + " | deviceToken: " + item.getToken());
        UserDeviceToken existedDeviceToken = userDeviceTokenRepository.findByUserIdAndToken(item.getUserId(), item.getToken());
        if (Objects.nonNull(existedDeviceToken)) {
            userDeviceTokenRepository.delete(existedDeviceToken);
        }
    }
}
