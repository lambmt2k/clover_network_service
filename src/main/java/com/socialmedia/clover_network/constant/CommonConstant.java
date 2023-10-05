package com.socialmedia.clover_network.constant;

import org.springframework.stereotype.Component;

@Component
public class CommonConstant {

    public static final String HOST_EMAIL = "ytbbuilamsonla@gmail.com";
    public static final String ADMIN_ACCOUNT = "ADMIN";
    public static final String SECRET_KEY = "clovernetwork@23";
    public static final String VECTOR_VALUE = "clovernetwork@23";
    public static final String LOGIN_PAGE_URL = "https://clover-network-web.vercel.app/login";

    public class API {
        public static final String API_LOGIN = "/api/authenticate/login-by-email";
        public static final String API_SIGNUP = "/api/authenticate/signup-by-email";
        public static final String API_VERIFY_ACCOUNT = "/api/authenticate/verify";
    }
}
