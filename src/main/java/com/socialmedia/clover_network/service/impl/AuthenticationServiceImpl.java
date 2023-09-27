package com.socialmedia.clover_network.service.impl;

import com.socialmedia.clover_network.constant.CommonConstant;
import com.socialmedia.clover_network.constant.CommonMessage;
import com.socialmedia.clover_network.constant.CommonRegex;
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
import org.springframework.http.HttpStatus;
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
                    //generate token
                    TokenItem tokenItem = this.genTokenItem(existedUserInfo, request);

                    UserLoginRes userLoginRes = new UserLoginRes();
                    userLoginRes.setUserId(tokenItem.getUserId());
                    userLoginRes.setTokenId(tokenItem.getTokenId());
                    userLoginRes.setUserRole(tokenItem.getUserRole());
                    userLoginRes.setExpireTime(tokenItem.getExpireTime());

                    res.setStatus(HttpStatus.OK.value());
                    res.setMessage(HttpStatus.OK.getReasonPhrase());
                    res.setCode(String.valueOf(HttpStatus.OK.value()));
                    res.setData(userLoginRes);
                } else {
                    res.setStatus(HttpStatus.UNAUTHORIZED.value());
                    res.setMessage("Wrong password");
                    res.setCode(String.valueOf(HttpStatus.UNAUTHORIZED.value()));
                    res.setData(req);
                }

            } else {
                res.setStatus(HttpStatus.NOT_FOUND.value());
                res.setMessage(HttpStatus.NOT_FOUND.getReasonPhrase());
                res.setCode(String.valueOf(HttpStatus.NOT_FOUND.value()));
                res.setData(req);
            }
        } else {
            res.setStatus(HttpStatus.BAD_REQUEST.value());
            res.setMessage(HttpStatus.BAD_REQUEST.getReasonPhrase());
            res.setCode(String.valueOf(HttpStatus.BAD_REQUEST.value()));
            res.setData(req);

        }
        return res;
    }

    private TokenItem genTokenItem(UserInfo userInfo, HttpServletRequest request) {
        LocalDateTime now = LocalDateTime.now();
        TokenItem res = new TokenItem();
        if (userInfo != null) {
            List<TokenItem> tokenItems = tokenItemRepository.findByUserId(userInfo.getUserId())
                    .stream()
                    .sorted(Comparator.comparing(TokenItem::getExpireTime).reversed())
                    .collect(Collectors.toList());
            if (tokenItems.size() > 0 && tokenItems.get(0).getExpireTime().isAfter(now)) {
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
            res.setStatus(HttpStatus.BAD_REQUEST.value());
            res.setData(null);
            res.setMessage(HttpStatus.BAD_REQUEST.getReasonPhrase());
            return res;
        }
        //verify input data
        Optional<UserInfo> userInfoOpt = userInfoRepository.findByEmail(req.getEmail());
        Optional<UserAuth> userAuthOpt = userAuthRepository.findByEmail(req.getEmail());
        if (userInfoOpt.isPresent() || userAuthOpt.isPresent()) {
            res.setStatus(HttpStatus.CONFLICT.value());
            res.setData(req.getEmail());
            res.setMessage(HttpStatus.CONFLICT.getReasonPhrase());
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
            res.setStatus(HttpStatus.OK.value());
            res.setMessage(CommonMessage.ResponseMessage.ACTION_SUCCESS);
            res.setData(userInfoRes);
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
            res.setCode(null);
            res.setData(userInfoOpt.get());
            res.setStatus(HttpStatus.OK.value());
            res.setMessage(HttpStatus.OK.getReasonPhrase());
        } else {
            res.setCode(null);
            res.setData(null);
            res.setStatus(HttpStatus.NOT_FOUND.value());
            res.setMessage(HttpStatus.NOT_FOUND.getReasonPhrase());
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
                res.setCode(null);
                res.setData(null);
                res.setStatus(HttpStatus.OK.value());
                res.setMessage(HttpStatus.OK.getReasonPhrase());
            } else {
                res.setCode(null);
                res.setData(null);
                res.setStatus(HttpStatus.NOT_FOUND.value());
                res.setMessage(HttpStatus.NOT_FOUND.getReasonPhrase());
            }
        } else {
            res.setStatus(HttpStatus.FORBIDDEN.value());
            res.setMessage(HttpStatus.FORBIDDEN.getReasonPhrase());
            res.setData(null);
            res.setCode(null);
        }
        /*Optional<TokenItem> tokenItemOpt = tokenItemRepository.findByTokenId(tokenId);
        if (tokenItemOpt.isPresent()) {
            res.setCode(null);
            res.setData(tokenItemOpt.get().getUserId());
            res.setStatus(HttpStatus.OK.value());
            res.setMessage(HttpStatus.OK.getReasonPhrase());
        } else {
            res.setCode(null);
            res.setData(null);
            res.setStatus(HttpStatus.NOT_FOUND.value());
            res.setMessage(HttpStatus.NOT_FOUND.getReasonPhrase());
        }*/
        return res;
    }

    @Override
    public TokenItem getTokenItem(String tokenId) {
        logger.info("[getTokenItem] Start get token: " + tokenId);
        Optional<TokenItem> tokenItem = tokenItemRepository.findByTokenId(tokenId);
        return tokenItem.orElseGet(() -> null);
    }
}
