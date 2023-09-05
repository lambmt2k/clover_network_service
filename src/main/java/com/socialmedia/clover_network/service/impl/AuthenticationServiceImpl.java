package com.socialmedia.clover_network.service.impl;

import com.socialmedia.clover_network.config.JwtRequestFilter;
import com.socialmedia.clover_network.config.JwtTokenUtil;
import com.socialmedia.clover_network.dto.req.UserLoginReq;
import com.socialmedia.clover_network.dto.req.UserSignUpReq;
import com.socialmedia.clover_network.entity.TokenItem;
import com.socialmedia.clover_network.entity.UserAuth;
import com.socialmedia.clover_network.entity.UserInfo;
import com.socialmedia.clover_network.enumuration.AccountType;
import com.socialmedia.clover_network.enumuration.UserStatus;
import com.socialmedia.clover_network.repository.UserAuthRepository;
import com.socialmedia.clover_network.repository.UserInfoRepository;
import com.socialmedia.clover_network.service.AuthenticationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class AuthenticationServiceImpl implements AuthenticationService {

    private static Logger logger = LoggerFactory.getLogger(AuthenticationServiceImpl.class);

    private final UserInfoRepository userInfoRepository;
    private final UserAuthRepository userAuthRepository;
    private final JwtTokenUtil jwtTokenUtil;
    private final AuthenticationManager authenticationManager;

    public AuthenticationServiceImpl(UserInfoRepository userInfoRepository,
                                     UserAuthRepository userAuthRepository,
                                     JwtTokenUtil jwtTokenUtil,
                                     AuthenticationManager authenticationManager) {
        this.userInfoRepository = userInfoRepository;
        this.userAuthRepository = userAuthRepository;
        this.jwtTokenUtil = jwtTokenUtil;
        this.authenticationManager = authenticationManager;
    }


    @Override
    public TokenItem loginByEmail(UserLoginReq req) throws Exception {
        String decryptPassword = decryptPassword(req.getPassword().trim());
        authenticate(req.getEmail().trim(), decryptPassword);
        //generate token
            String token = jwtTokenUtil.generateToken(req.getEmail());
        return null;
    }
    @PostMapping(API_Config.API_LOGIN_URL)
    public ResponseEntity<AccountInfoRp> loginAuthenticationToken(@RequestBody AccountInfoRq requestInfo ) throws Exception {
        //authentication
        boolean isChecked = !requestInfo.getData().getUsername().isEmpty() && !requestInfo.getData().getPassword().isEmpty();
        AccountInfoRp accountRp = new AccountInfoRp();
        AccountDataRp accountDbRp = new AccountDataRp();
        AuthToken authentk = new AuthToken();
        if (isChecked) {
            String dryptPass = decyptPass(requestInfo.getData().getPassword().trim());
            authenticate(requestInfo.getData().getUsername().trim(), dryptPass);
            //generate token
            String token = jwtTokenUtil.generateToken(requestInfo.getData().getUsername());
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
        }
    }

    private String decryptPassword(String originPassword) {
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
            newUserInfo.setAccountType(AccountType.EMAIL);

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
            throw new Exception(e);
        }catch (BadCredentialsException e){
            throw new Exception(e);
        }
    }
    @GetMapping(API_Config.API_INFO_URL)
    public ResponseEntity<Object> getInfo(){
        ApiResponse response = new ApiResponse();
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        AccountUserInfo info = accountInfoService.getInfoUserById(userId);
        logger.info("[Get info] userId : " + userId);
        if(info != null){
            response.setCode(MessageCode.LOGIN_SUCCESSFUL.getErrorcode());
            response.setData(info);
            response.setStatus_code(Integer.parseInt(Config.MESSAGE_STATUS.OK.getValue()));
            response.setMessage(ssoErrorMessageRepository.findByErrorCode(MessageCode.LOGIN_SUCCESSFUL.getErrorcode()).getDescriptionVn());
            return new ResponseEntity<Object>(response, HttpStatus.OK);
        }else{
            response.setCode(MessageCode.PORTAL_NODATAFIND.name());
            response.setData(null);
            response.setStatus(HttpStatus.NOT_FOUND);
            response.setMessage(Config.MESSAGE_TEXT.NOT_FOUND.getValue());
            return new ResponseEntity<Object>(response, HttpStatus.OK);
        }
    }

    public String decryptWithKey(String decryptKey,String encryptText) {
        String result = "";

        try {
            logger.info("********************* START DECRYPT TRIPLEDES *********************");


            TripleDesEncryptionUtil tripleDesEncryptionUtil = new TripleDesEncryptionUtil();
            result = tripleDesEncryptionUtil.decrypt(decryptKey, encryptText);

            logger.info("********************* END DECRYPT TRIPLEDES *********************");

        } catch (Exception e) {
            e.printStackTrace();
            result = e.toString();
        }
        return result;
    }

    public String encryptWithKey(String encryptKey ,String plainText) {
        String result = "";

        try {
            logger.info("********************* START ENCRYPT TRIPLEDES *********************");

            TripleDesEncryptionUtil tripleDesEncryptionUtil = new TripleDesEncryptionUtil();
            result = tripleDesEncryptionUtil.encrypt(encryptKey,plainText);

            logger.info("********************* END ENCRYPT TRIPLEDES *********************");

        } catch (Exception e) {
            e.printStackTrace();
            result = e.toString();
        }

        return result;
    }
}
