package com.socialmedia.clover_network.constant;

import org.springframework.stereotype.Component;

@Component
public class CommonConstant {
    public class API {
        public static final String API_LOGIN = "/api/authenticate/login-by-email";
        public static final String API_SIGNUP = "/api/authenticate/signup-by-email";
    }
}
