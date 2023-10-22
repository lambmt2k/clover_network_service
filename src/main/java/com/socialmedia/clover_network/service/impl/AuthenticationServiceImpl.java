package com.socialmedia.clover_network.service.impl;

import com.socialmedia.clover_network.constant.CommonConstant;
import com.socialmedia.clover_network.constant.CommonRegex;
import com.socialmedia.clover_network.constant.ErrorCode;
import com.socialmedia.clover_network.dto.req.UserLoginReq;
import com.socialmedia.clover_network.dto.req.UserSignUpReq;
import com.socialmedia.clover_network.dto.res.ApiResponse;
import com.socialmedia.clover_network.dto.res.UserInfoRes;
import com.socialmedia.clover_network.dto.res.UserLoginRes;
import com.socialmedia.clover_network.entity.TokenItem;
import com.socialmedia.clover_network.entity.UserAuth;
import com.socialmedia.clover_network.entity.UserInfo;
import com.socialmedia.clover_network.enumuration.AccountType;
import com.socialmedia.clover_network.enumuration.Gender;
import com.socialmedia.clover_network.enumuration.UserRole;
import com.socialmedia.clover_network.enumuration.UserStatus;
import com.socialmedia.clover_network.repository.TokenItemRepository;
import com.socialmedia.clover_network.repository.UserAuthRepository;
import com.socialmedia.clover_network.repository.UserInfoRepository;
import com.socialmedia.clover_network.service.AuthenticationService;
import com.socialmedia.clover_network.service.MailService;
import com.socialmedia.clover_network.service.UserWallService;
import com.socialmedia.clover_network.util.EncryptUtil;
import com.socialmedia.clover_network.util.GenIDUtil;
import com.socialmedia.clover_network.util.HttpHelper;
import com.socialmedia.clover_network.util.JwtTokenUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class AuthenticationServiceImpl implements AuthenticationService {

    private final Logger logger = LoggerFactory.getLogger(AuthenticationServiceImpl.class);

    private final UserInfoRepository userInfoRepository;
    private final UserAuthRepository userAuthRepository;
    private final TokenItemRepository tokenItemRepository;

    private final JwtTokenUtil jwtTokenUtil;
    private final GenIDUtil genIDUtil;

    private final MailService mailService;
    private final UserWallService userWallService;

    public AuthenticationServiceImpl(UserInfoRepository userInfoRepository,
                                     UserAuthRepository userAuthRepository,
                                     TokenItemRepository tokenItemRepository,
                                     JwtTokenUtil jwtTokenUtil,
                                     GenIDUtil genIDUtil,
                                     MailService mailService,
                                     UserWallService userWallService) {
        this.userInfoRepository = userInfoRepository;
        this.userAuthRepository = userAuthRepository;
        this.tokenItemRepository = tokenItemRepository;
        this.jwtTokenUtil = jwtTokenUtil;
        this.genIDUtil = genIDUtil;
        this.mailService = mailService;
        this.userWallService = userWallService;
    }


    @Override
    public ApiResponse loginByEmail(HttpServletRequest request, UserLoginReq req) throws Exception {
        logger.info("Start [loginByEmail] has email: {}", req.getEmail());
        ApiResponse res = new ApiResponse();
        LocalDateTime now = LocalDateTime.now();
        if (!req.getEmail().contains(CommonRegex.REGEX_EMAIL)) {
            res.setCode(ErrorCode.Authentication.INVALID_DATA.getCode());
            res.setData(req);
            res.setMessageEN(ErrorCode.Authentication.INVALID_DATA.getMessageEN());
            res.setMessageVN(ErrorCode.Authentication.INVALID_DATA.getMessageVN());
            return res;
        }
        boolean isChecked = !req.getEmail().isEmpty() && !req.getPassword().isEmpty();
        if (isChecked) {
            String encryptedPassword = EncryptUtil.encrypt(req.getPassword().trim());
            /*authenticate(req.getEmail().trim(), encryptedPassword);*/
            //find userinfo
            Optional<UserInfo> userInfoOpt = userInfoRepository.findByEmail(req.getEmail());
            Optional<UserAuth> userAuthOpt = userAuthRepository.findByEmail(req.getEmail());
            if (userAuthOpt.isPresent() && userInfoOpt.isPresent()) {
                UserInfo existedUserInfo = userInfoOpt.get();
                UserAuth existedUserAuth = userAuthOpt.get();
                if (existedUserAuth.getPassword().equals(encryptedPassword)) {
                    switch (existedUserInfo.getStatus()) {
                        case INACTIVE: {
                            TokenItem tokenItem = this.genTokenItem(existedUserInfo, request);

                            //send mail active account if token
                            mailService.sendMailActiveAccount(existedUserInfo, tokenItem.getTokenId());

                            res.setCode(ErrorCode.Authentication.ACCOUNT_NOT_ACTIVE.getCode());
                            res.setData(req);
                            res.setMessageEN(ErrorCode.Authentication.ACCOUNT_NOT_ACTIVE.getMessageEN());
                            res.setMessageVN(ErrorCode.Authentication.ACCOUNT_NOT_ACTIVE.getMessageVN());
                            break;
                        }
                        case ACTIVE: {
                            //generate token
                            TokenItem tokenItem = this.genTokenItem(existedUserInfo, request);

                            UserLoginRes userLoginRes = new UserLoginRes();
                            userLoginRes.setUserId(tokenItem.getUserId());
                            userLoginRes.setTokenId(tokenItem.getTokenId());
                            userLoginRes.setUserRole(tokenItem.getUserRole());
                            userLoginRes.setExpireTime(tokenItem.getExpireTime());

                            res.setCode(ErrorCode.Authentication.ACTION_SUCCESS.getCode());
                            res.setData(userLoginRes);
                            res.setMessageEN(ErrorCode.Authentication.ACTION_SUCCESS.getMessageEN());
                            res.setMessageVN(ErrorCode.Authentication.ACTION_SUCCESS.getMessageVN());
                            break;
                        }
                    }
                } else {
                    res.setCode(ErrorCode.Authentication.AUTHEN_ERROR.getCode());
                    res.setData(req);
                    res.setMessageEN(ErrorCode.Authentication.AUTHEN_ERROR.getMessageEN());
                    res.setMessageVN(ErrorCode.Authentication.AUTHEN_ERROR.getMessageVN());
                }
            } else {
                res.setCode(ErrorCode.User.PROFILE_GET_EMPTY.getCode());
                res.setData(req);
                res.setMessageEN(ErrorCode.User.PROFILE_GET_EMPTY.getMessageEN());
                res.setMessageVN(ErrorCode.User.PROFILE_GET_EMPTY.getMessageVN());
            }
        } else {
            res.setCode(ErrorCode.Authentication.AUTHEN_ERROR.getCode());
            res.setData(req);
            res.setMessageEN(ErrorCode.Authentication.AUTHEN_ERROR.getMessageEN());
            res.setMessageVN(ErrorCode.Authentication.AUTHEN_ERROR.getMessageVN());
        }
        return res;
    }

    @Override
    public ApiResponse getAllUserInfo(String userId) throws Exception {
        ApiResponse res = new ApiResponse();
        if (userId != null && userId.equals("lambmt")) {
            List<UserInfo> allUserInfo = userInfoRepository.findByUserStatus(UserStatus.ACTIVE);
            res.setCode(ErrorCode.Authentication.ACTION_SUCCESS.getCode());
            res.setData(allUserInfo);
            res.setMessageEN(ErrorCode.Authentication.ACTION_SUCCESS.getMessageEN());
            res.setMessageVN(ErrorCode.Authentication.ACTION_SUCCESS.getMessageVN());
        } else {
            res.setCode(ErrorCode.Authentication.AUTHEN_ERROR.getCode());
            res.setData(userId);
            res.setMessageEN(ErrorCode.Authentication.AUTHEN_ERROR.getMessageEN());
            res.setMessageVN(ErrorCode.Authentication.AUTHEN_ERROR.getMessageVN());
        }
        return res;
    }

    private TokenItem genTokenItem(UserInfo userInfo, HttpServletRequest request) {
        LocalDateTime now = LocalDateTime.now();
        TokenItem res = new TokenItem();
        if (userInfo != null) {
            List<TokenItem> tokenItems = tokenItemRepository.findByUserId(userInfo.getUserId())
                    .stream()
                    .filter(tokenItem -> !tokenItem.isDelFlag())
                    .sorted(Comparator.comparing(TokenItem::getExpireTime).reversed())
                    .collect(Collectors.toList());
            if (tokenItems.size() > 0 && tokenItems.get(0).getExpireTime().isAfter(now) && !tokenItems.get(0).isDelFlag()) {
                //get old token
                res = tokenItems.get(0);
            } else {
                //gen new token
                HttpHelper httpHelper = new HttpHelper(request);
                String tokenId = jwtTokenUtil.generateToken(userInfo.getUserId());
                res = TokenItem
                        .builder()
                        .tokenId(tokenId)
                        .userId(userInfo.getUserId())
                        .userRole(userInfo.getUserRole())
                        .userIp(httpHelper.getClientIp())
                        .userAgent(httpHelper.getUserAgent())
                        .os(TokenItem.OS.WINDOWS)
                        .createdTime(now)
                        .expireTime(now.plus(90, ChronoUnit.DAYS))
                        .delFlag(false)
                        .build();
                tokenItemRepository.save(res);
            }
        }
        return res;
    }

    @Override
    public ApiResponse signUpNewUser(HttpServletRequest request, UserSignUpReq req) throws Exception {
        logger.info("Start [signUpNewUser]");
        ApiResponse res = new ApiResponse();
        SimpleDateFormat dateFormat = new SimpleDateFormat(CommonRegex.PATTERN_DATE.pattern());
        if (!req.getEmail().contains(CommonRegex.REGEX_EMAIL)) {
            res.setCode(ErrorCode.Authentication.INVALID_DATA.getCode());
            res.setData(req);
            res.setMessageEN(ErrorCode.Authentication.INVALID_DATA.getMessageEN());
            res.setMessageVN(ErrorCode.Authentication.INVALID_DATA.getMessageVN());
            return res;
        }
        //verify input data
        Optional<UserInfo> userInfoOpt = userInfoRepository.findByEmail(req.getEmail());
        Optional<UserAuth> userAuthOpt = userAuthRepository.findByEmail(req.getEmail());
        if (userInfoOpt.isPresent() || userAuthOpt.isPresent()) {
            res.setCode(ErrorCode.User.EXISTED_USER.getCode());
            res.setData(req.getEmail());
            res.setMessageEN(ErrorCode.User.EXISTED_USER.getMessageEN());
            res.setMessageVN(ErrorCode.User.EXISTED_USER.getMessageVN());
            return res;
        } else {
            LocalDateTime now = LocalDateTime.now();
            String newUserId = genIDUtil.genId();

            //create new user info
            UserInfo newUserInfo = new UserInfo();
            newUserInfo.setUserId(newUserId);
            newUserInfo.setAvatarImgUrl(null);
            newUserInfo.setBannerImgUrl(null);
            newUserInfo.setEmail(req.getEmail());
            newUserInfo.setFirstname(req.getFirstname());
            newUserInfo.setLastname(req.getLastname());
            newUserInfo.setDisplayName(req.getFirstname() + CommonRegex.REGEX_SPACE + req.getLastname());
            newUserInfo.setDayOfBirth(req.getDayOfBirth());
            newUserInfo.setPhoneNo(null);
            newUserInfo.setGender(req.getGender());
            newUserInfo.setAccountType(AccountType.EMAIL);
            newUserInfo.setUserRole(UserRole.USER);
            newUserInfo.setStatus(UserStatus.INACTIVE);
            newUserInfo.setCreatedBy(CommonConstant.ADMIN_ACCOUNT);
            newUserInfo.setCreatedDate(now);
            newUserInfo.setUpdatedBy(CommonConstant.ADMIN_ACCOUNT);
            newUserInfo.setUpdatedDate(now);
            newUserInfo.setAccountType(AccountType.EMAIL);

            //create new user auth
            UserAuth newUserAuth = new UserAuth();
            newUserAuth.setUserId(newUserId);
            newUserAuth.setEmail(req.getEmail());
            String encryptedPassword = EncryptUtil.encrypt(req.getPassword());
            newUserAuth.setPassword(encryptedPassword);

            //save into database
            userInfoRepository.save(newUserInfo);
            userAuthRepository.save(newUserAuth);

            //gen token
            HttpHelper httpHelper = new HttpHelper(request);
            String tokenId = jwtTokenUtil.generateToken(newUserAuth.getUserId());
            TokenItem tokenItem = TokenItem
                    .builder()
                    .tokenId(tokenId)
                    .userId(newUserInfo.getUserId())
                    .userRole(newUserInfo.getUserRole())
                    .userIp(httpHelper.getClientIp())
                    .userAgent(httpHelper.getUserAgent())
                    .os(TokenItem.OS.WINDOWS)
                    .createdTime(now)
                    .expireTime(now.plus(90, ChronoUnit.DAYS))
                    .delFlag(false)
                    .build();
            tokenItemRepository.save(tokenItem);

            //send mail active account
            mailService.sendMailActiveAccount(newUserInfo, tokenId);

            //create new user's wall
            String newUserWallId = userWallService.createUserWall(newUserId);

            UserInfoRes userInfoRes = new UserInfoRes();
            userInfoRes.setEmail(newUserInfo.getEmail());
            userInfoRes.setFirstname(newUserInfo.getFirstname());
            userInfoRes.setLastname(newUserInfo.getLastname());
            userInfoRes.setGender(
                    newUserInfo.getGender().equals(Gender.MALE) ? "MALE"
                            : (newUserInfo.getGender().equals(Gender.FEMALE) ? "FEMALE" : "OTHER"));
            userInfoRes.setUserRole(newUserInfo.getUserRole().getRoleName());
            userInfoRes.setDayOfBirth(dateFormat.format(newUserInfo.getDayOfBirth()));
            userInfoRes.setStatus(newUserInfo.getStatus().getStatusName());

            res.setCode(ErrorCode.User.ACTION_SUCCESS.getCode());
            res.setData(userInfoRes);
            res.setMessageEN(ErrorCode.User.ACTION_SUCCESS.getMessageEN());
            res.setMessageVN(ErrorCode.User.ACTION_SUCCESS.getMessageVN());

        }
        logger.info("Finish [signUpNewUser]");
        return res;
    }

    @Override
    public ApiResponse getUserInfo() {
        logger.info("Start [getUserInfo]");
        ApiResponse res = new ApiResponse();
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Optional<UserInfo> userInfoOpt = userInfoRepository.findByEmail(email);
        if (userInfoOpt.isPresent()) {
            res.setCode(ErrorCode.User.ACTION_SUCCESS.getCode());
            res.setData(userInfoOpt.get());
            res.setMessageEN(ErrorCode.User.ACTION_SUCCESS.getMessageEN());
            res.setMessageVN(ErrorCode.User.ACTION_SUCCESS.getMessageVN());
        } else {
            res.setCode(ErrorCode.User.PROFILE_GET_EMPTY.getCode());
            res.setData(null);
            res.setMessageEN(ErrorCode.User.PROFILE_GET_EMPTY.getMessageEN());
            res.setMessageVN(ErrorCode.User.PROFILE_GET_EMPTY.getMessageVN());
        }
        return res;
    }

    @Override
    public ApiResponse verifyAccount(String tokenId) {
        logger.info("Start [verifyAccount]");
        ApiResponse res = new ApiResponse();
        Optional<TokenItem> tokenItemOpt = tokenItemRepository.findByTokenId(tokenId);
        if (tokenItemOpt.isPresent()) {
            TokenItem tokenItem = tokenItemOpt.get();
            logger.info("Verify account has userId: {}", tokenItem.getUserId());
            Optional<UserInfo> userInfoOpt = userInfoRepository.findByUserId(tokenItem.getUserId());
            if (userInfoOpt.isPresent()) {
                UserInfo existedUser = userInfoOpt.get();
                existedUser.setStatus(UserStatus.ACTIVE);
                userInfoRepository.save(existedUser);
                res.setCode(ErrorCode.User.ACTION_SUCCESS.getCode());
                res.setData(null);
                res.setMessageEN(ErrorCode.User.ACTION_SUCCESS.getMessageEN());
                res.setMessageVN(ErrorCode.User.ACTION_SUCCESS.getMessageVN());
            } else {
                res.setCode(ErrorCode.User.PROFILE_GET_EMPTY.getCode());
                res.setData(null);
                res.setMessageEN(ErrorCode.User.PROFILE_GET_EMPTY.getMessageEN());
                res.setMessageVN(ErrorCode.User.PROFILE_GET_EMPTY.getMessageVN());
            }
        } else {
            res.setCode(ErrorCode.Token.FORBIDDEN.getCode());
            res.setData(null);
            res.setMessageEN(ErrorCode.Token.FORBIDDEN.getMessageEN());
            res.setMessageVN(ErrorCode.Token.FORBIDDEN.getMessageVN());
        }
        return res;
    }

    @Override
    public TokenItem getTokenItem(String tokenId) {
        logger.info("[getTokenItem] Start get token: " + tokenId);
        Optional<TokenItem> tokenItem = tokenItemRepository.findByTokenId(tokenId);
        return tokenItem.orElse(null);
    }
}
