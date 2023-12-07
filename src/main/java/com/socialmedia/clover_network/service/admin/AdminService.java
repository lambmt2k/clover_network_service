package com.socialmedia.clover_network.service.admin;

import com.socialmedia.clover_network.config.AuthenticationHelper;
import com.socialmedia.clover_network.entity.TokenItem;
import com.socialmedia.clover_network.entity.UserInfo;
import com.socialmedia.clover_network.enumuration.UserRole;
import com.socialmedia.clover_network.repository.TokenItemRepository;
import com.socialmedia.clover_network.repository.UserInfoRepository;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class AdminService {

    @Autowired
    UserInfoRepository userInfoRepository;
    @Autowired
    TokenItemRepository tokenItemRepository;

    private final Logger logger = LoggerFactory.getLogger(AdminService.class);

    public TokenItem getTokenByUserId(String userId) {
        String currentUserId = AuthenticationHelper.getUserIdFromContext();
        if (StringUtils.isEmpty(currentUserId)) {
            return null;
        }
        Optional<UserInfo> userInfoOpt = userInfoRepository.findByUserId(currentUserId);
        if (userInfoOpt.isEmpty() || !userInfoOpt.get().getUserRole().equals(UserRole.ADMIN)) {
            return null;
        }
        List<TokenItem> tokenItems = tokenItemRepository.findByUserIdAndDelFlagFalseOrderByCreatedTimeDesc(userId);
        if (tokenItems.isEmpty()) {
            return null;
        } else {
            return tokenItems.get(0);
        }

    }

    public UserInfo getUserInfoByEmail(String email) {
        String currentUserId = AuthenticationHelper.getUserIdFromContext();
        if (StringUtils.isEmpty(currentUserId)) {
            return null;
        }
        Optional<UserInfo> userInfoOpt = userInfoRepository.findByUserId(currentUserId);
        if (userInfoOpt.isEmpty() || !userInfoOpt.get().getUserRole().equals(UserRole.ADMIN)) {
            return null;
        }

        return userInfoRepository.findByEmail(email).orElse(null);

    }
}
