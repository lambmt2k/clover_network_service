package com.socialmedia.clover_network.service;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.*;
import com.google.firebase.cloud.StorageClient;
import com.socialmedia.clover_network.enumuration.ImageType;
import com.socialmedia.clover_network.util.GenIDUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class FirebaseService {
    @Value("${app_setting.firebase.bucket_name}")
    private String bucketName;

    @Autowired
    GenIDUtil genIDUtil;

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

    public String getImageUrl(String imagePath) {
        return StorageClient.getInstance().bucket().get(imagePath).signUrl(1, TimeUnit.HOURS).toString();
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
}
