package com.socialmedia.clover_network.constant;

import org.springframework.stereotype.Component;

@Component
public class CommonConstant {

    public static final String HOST_EMAIL = "ytbbuilamsonla@gmail.com";
    public static final String ADMIN_ACCOUNT = "ADMIN";
    public static final String SECRET_KEY = "clovernetwork@23";
    public static final String VECTOR_VALUE = "clovernetwork@23";
    public static final String LOGIN_PAGE_URL = "https://clover-network-web.vercel.app/login";
    public static final String DEFAULT_AVATAR_URL = "https://firebasestorage.googleapis.com/v0/b/clover-network-afd47.appspot.com/o/default_avatar.jpg?alt=media&token=07959b46-9cfa-4e5c-8b82-5e8d6b05336a&_gl=1*akeqku*_ga*MTk4Mzg4ODQyNy4xNjk2NTMzMzMw*_ga_CW55HF8NVT*MTY5OTI5NDYyNC40LjEuMTY5OTI5NDgwNy41Mi4wLjA.";

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
