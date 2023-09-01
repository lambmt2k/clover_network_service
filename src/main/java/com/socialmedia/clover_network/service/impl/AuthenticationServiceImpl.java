package com.socialmedia.clover_network.service.impl;

import com.socialmedia.clover_network.config.JwtRequestFilter;
import com.socialmedia.clover_network.config.JwtTokenUtil;
import com.socialmedia.clover_network.dto.req.UserLoginReq;
import com.socialmedia.clover_network.dto.req.UserSignUpReq;
import com.socialmedia.clover_network.entity.TokenItem;
import com.socialmedia.clover_network.entity.UserAuth;
import com.socialmedia.clover_network.entity.UserInfo;
import com.socialmedia.clover_network.enumuration.UserStatus;
import com.socialmedia.clover_network.repository.UserAuthRepository;
import com.socialmedia.clover_network.repository.UserInfoRepository;
import com.socialmedia.clover_network.service.AuthenticationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class AuthenticationServiceImpl implements AuthenticationService {

    private static Logger logger = LoggerFactory.getLogger(JwtRequestFilter.class);

    private final UserInfoRepository userInfoRepository;
    private final UserAuthRepository userAuthRepository;
    private final JwtTokenUtil jwtTokenUtil;

    public AuthenticationServiceImpl(UserInfoRepository userInfoRepository,
                                     UserAuthRepository userAuthRepository, JwtTokenUtil jwtTokenUtil) {
        this.userInfoRepository = userInfoRepository;
        this.userAuthRepository = userAuthRepository;
        this.jwtTokenUtil = jwtTokenUtil;
    }


    @Override
    public TokenItem loginByEmail(UserLoginReq req) throws Exception {
        String dryptPass = decyptPass(req.getPassword().trim());
        authenticate(req.getEmail().trim(), dryptPass);
        //generate token
            String token = jwtTokenUtil.generateToken(req.getEmail());
        return null;
    }

    @Override
    public void signUpNewUser(UserSignUpReq req) {

        //verify input data

        Optional<UserInfo> userInfoOtp = userInfoRepository.findByEmail(req.getEmail());
        Optional<UserAuth> userAuthOtp = userAuthRepository.findByEmail(req.getEmail());
        if (userInfoOtp.isPresent() || userAuthOtp.isPresent()) {

        } else {
            LocalDateTime now = LocalDateTime.now();

            //create new user info
            UserInfo newUserInfo = new UserInfo();
            newUserInfo.setEmail(req.getEmail());
            newUserInfo.setFirstname(req.getFirstname());
            newUserInfo.setLastname(req.getLastname());
            newUserInfo.setDayOfBirth(req.getDayOfBirth());
            newUserInfo.setGender(req.getGender());
            newUserInfo.setStatus(UserStatus.INACTIVE);
            newUserInfo.setCreatedBy("anonymous");
            newUserInfo.setCreatedDate(now);
            newUserInfo.setUpdatedBy("anonymous");
            newUserInfo.setUpdatedDate(now);

            //create new user auth
            UserAuth newUserAuth = new UserAuth();
            newUserAuth.setEmail(req.getEmail());
            newUserAuth.setPassword(req.getPassword());

            userInfoRepository.save(newUserInfo);
            userAuthRepository.save(newUserAuth);
        }
    }
    private void authenticate(String email , String password) throws Exception {
        logger.info("authenticate");
        try{
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(email,password));
        }catch (DisabledException e){
            throw new Exception(Config.ERROR.USER_DISABLE.getValue(),e);
        }catch (BadCredentialsException e){
            throw new Exception(Config.ERROR.INVALID_CREDNTIALS.getValue(),e);
        }
    }
}
