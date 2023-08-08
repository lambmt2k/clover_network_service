package com.socialmedia.clover_network.service.impl;

import com.socialmedia.clover_network.controller.dto.UserLoginReq;
import com.socialmedia.clover_network.controller.dto.UserLoginRes;
import com.socialmedia.clover_network.entity.UserAuth;
import com.socialmedia.clover_network.repository.UserAuthRepo;
import com.socialmedia.clover_network.service.UserService;
import com.socialmedia.clover_network.util.ResponseCode;
import com.socialmedia.clover_network.util.SystemException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserServiceImpl implements UserService {
    @Autowired
    private UserAuthRepo userAuthRepo;

    @Override
    public UserLoginRes login(UserLoginReq req) throws SystemException {
        UserLoginRes userLoginRes = new UserLoginRes();

        Optional<UserAuth> userAuth = userAuthRepo.findByEmail(req.getEmail());

        if (userAuth.isEmpty()) {
            throw new SystemException(ResponseCode.UserAuthError.USER_AUTH_NOT_EXIST);
        }

        UserAuth user = userAuth.get();
        if (!req.getPassword().equals(user.getPassword())) {
            throw new SystemException(ResponseCode.UserAuthError.USER_AUTH_DEFAULT);
        }

        userLoginRes.setUserId(user.getUser().getId());

        return userLoginRes;
    }
}
