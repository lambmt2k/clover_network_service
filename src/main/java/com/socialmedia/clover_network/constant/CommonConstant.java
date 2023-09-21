package com.socialmedia.clover_network.constant;

import org.springframework.stereotype.Component;

@Component
public class CommonConstant {

    public static final String HOST_EMAIL = "lambmt.work@gmail.com";
    public static final String ADMIN_ACCOUNT = "ADMIN";
    public static final String ENCRYPT_KEY = "clovernetwork";

    public class API {
        public static final String API_LOGIN = "/api/authenticate/login-by-email";
        public static final String API_SIGNUP = "/api/authenticate/signup-by-email";
        public static final String API_VERIFY_ACCOUNT = "/api/authenticate//verify";
    }
}
