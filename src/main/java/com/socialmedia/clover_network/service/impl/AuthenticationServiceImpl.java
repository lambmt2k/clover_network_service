package com.socialmedia.clover_network.service.impl;

import com.socialmedia.clover_network.config.JwtTokenUtil;
import com.socialmedia.clover_network.constant.CommonConstant;
import com.socialmedia.clover_network.constant.CommonMessage;
import com.socialmedia.clover_network.constant.CommonRegex;
import com.socialmedia.clover_network.dto.req.UserLoginReq;
import com.socialmedia.clover_network.dto.req.UserSignUpReq;
import com.socialmedia.clover_network.dto.res.ApiResponse;
import com.socialmedia.clover_network.dto.res.UserInfoRes;
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
import com.socialmedia.clover_network.util.EncryptUtil;
import com.socialmedia.clover_network.util.GenIDUtil;
import com.socialmedia.clover_network.util.HttpHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import javax.mail.MessagingException;
import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

@Service
public class AuthenticationServiceImpl implements AuthenticationService {

    private final Logger logger = LoggerFactory.getLogger(AuthenticationServiceImpl.class);

    private final UserInfoRepository userInfoRepository;
    private final UserAuthRepository userAuthRepository;
    private final TokenItemRepository tokenItemRepository;

    private final JwtTokenUtil jwtTokenUtil;
    private final GenIDUtil genIDUtil;
    private final AuthenticationManager authenticationManager;

    private final MailService mailService;

    public AuthenticationServiceImpl(UserInfoRepository userInfoRepository,
                                     UserAuthRepository userAuthRepository,
                                     TokenItemRepository tokenItemRepository,
                                     JwtTokenUtil jwtTokenUtil,
                                     GenIDUtil genIDUtil,
                                     AuthenticationManager authenticationManager,
                                     MailService mailService) {
        this.userInfoRepository = userInfoRepository;
        this.userAuthRepository = userAuthRepository;
        this.tokenItemRepository = tokenItemRepository;
        this.jwtTokenUtil = jwtTokenUtil;
        this.genIDUtil = genIDUtil;
        this.authenticationManager = authenticationManager;
        this.mailService = mailService;
    }


    @Override
    public TokenItem loginByEmail(UserLoginReq req) throws Exception {
        logger.info("Start [loginByEmail]");
        /*boolean isChecked = !req.getEmail().isEmpty() && !req.getPassword().isEmpty();
        AccountInfoRp accountRp = new AccountInfoRp();
        AccountDataRp accountDbRp = new AccountDataRp();
        AuthToken authentk = new AuthToken();
        if (isChecked) {
            String decryptPassword = decryptPassword(req.getPassword().trim());
            authenticate(req.getEmail().trim(), decryptPassword);
            //generate token
            String token = jwtTokenUtil.generateToken(req.getEmail());
            accountInfoService.saveAccountInfo(requestInfo.getData().getUsername(), requestInfo.getIp() , requestInfo.getDeviceId(), token, 1);
            accountRp.setCode(MessageCode.LOGIN_SUCCESSFUL.getErrorcode());
            accountRp.setMessage(ssoErrorMessageRepository.findByErrorCode(MessageCode.LOGIN_SUCCESSFUL.getErrorcode()).getDescriptionVn());
            authentk.setToken(token);
            accountDbRp.setAuth(authentk);
            //hard code user info - lên live sẽ thay đổi
            //accountDbRp.setAccountUserInfo(accountInfoService.getInfoUserById("DUCOH"));
            accountDbRp.setAccountUserInfo(accountInfoService.getInfoUserById(requestInfo.getData().getUsername().trim()));
            accountRp.setData(accountDbRp);
            accountRp.setStatus_code(Integer.parseInt(Config.MESSAGE_STATUS.OK.getValue()));
            return new ResponseEntity<AccountInfoRp>(accountRp, HttpStatus.OK);
        } else {
            accountRp.setCode(MessageCode.LOGIN_FAILED.getErrorcode());
            accountRp.setMessage(ssoErrorMessageRepository.findByErrorCode(MessageCode.LOGIN_FAILED.getErrorcode()).getDescriptionVn());
            accountRp.setStatus(HttpStatus.BAD_REQUEST);
            return new ResponseEntity<AccountInfoRp>(accountRp, HttpStatus.OK);
        }*/
        return null;
    }


    private String decryptPassword(String originPassword) {
        return null;
    }

    @Override
    public ApiResponse signUpNewUser(HttpServletRequest request, UserSignUpReq req) throws MessagingException, UnsupportedEncodingException {
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
            res.setMessage(HttpStatus.CONTINUE.getReasonPhrase());
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
            newUserAuth.setPassword(encryptWithKey(CommonConstant.ENCRYPT_KEY, req.getPassword()));

            //save into database
            userInfoRepository.save(newUserInfo);
            userAuthRepository.save(newUserAuth);

            HttpHelper httpHelper = new HttpHelper(request);
            String tokenId = jwtTokenUtil.generateToken(newUserAuth.getEmail());
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
                    .build();
            tokenItemRepository.save(tokenItem);

            //send mail active account
            mailService.sendMailActiveAccount(newUserInfo, tokenId);

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
            res.setMessage(CommonMessage.ResponseMessage.STATUS_200);
            res.setData(userInfoRes);
        }
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


    private void authenticate(String email, String password) throws Exception {
        logger.info("authenticate");
        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(email, password));
        } catch (DisabledException | BadCredentialsException e) {
            throw new Exception(e);
        }
    }


    public String decryptWithKey(String decryptKey, String encryptText) {
        String result;
        try {
            logger.info("********************* START DECRYPT TRIPLEDES *********************");
            EncryptUtil encryptUtil = new EncryptUtil();
            result = EncryptUtil.decrypt(decryptKey, encryptText);
            logger.info("********************* END DECRYPT TRIPLEDES *********************");
        } catch (Exception e) {
            e.printStackTrace();
            result = e.toString();
        }
        return result;
    }

    public String encryptWithKey(String encryptKey, String plainText) {
        String result;
        try {
            logger.info("********************* START ENCRYPT TRIPLEDES *********************");
            EncryptUtil encryptUtil = new EncryptUtil();
            result = EncryptUtil.encrypt(encryptKey, plainText);
            logger.info("********************* END ENCRYPT TRIPLEDES *********************");
        } catch (Exception e) {
            e.printStackTrace();
            result = e.toString();
        }
        return result;
    }
}
