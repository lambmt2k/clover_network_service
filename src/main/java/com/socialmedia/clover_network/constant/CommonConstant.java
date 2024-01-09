package com.socialmedia.clover_network.constant;

import org.springframework.stereotype.Component;

@Component
public class CommonConstant {

    public static final String HOST_EMAIL = "ytbbuilamsonla@gmail.com";
    public static final String ADMIN_ACCOUNT = "ADMIN";
    public static final String SECRET_KEY = "clovernetwork@23";
    public static final String VECTOR_VALUE = "clovernetwork@23";
    public static final String LOGIN_PAGE_URL = "https://clover-network-web.vercel.app/login";
    public static final String DEFAULT_AVATAR_URL = "images/user_avatar/default_avatar.jpg";
    public static final String DEFAULT_BANNER_URL = "images/group_banner/default_banner.jpg";
    public static final int OTP_LENGTH = 6;
    public static final long OTP_EXPIRATION_TIME_SECONDS = 150;
    public static final String SYSTEM_GROUP_ID = "1191451836392214528";

    public class API {
        public static final String API_LOGIN = "/api/authenticate/login-by-email";
        public static final String API_SIGNUP = "/api/authenticate/signup-by-email";
        public static final String API_VERIFY_ACCOUNT = "/api/authenticate/verify";
    }

    public class Feed {
        public static final int MAX_CHARACTER_CONTENT_FEED = 4000;
    }

    public class Indices {
        public static final String USER_INDEX = "user";
    }
}
