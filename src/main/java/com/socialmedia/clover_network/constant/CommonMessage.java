package com.socialmedia.clover_network.constant;

import org.springframework.stereotype.Component;

@Component
public class CommonMessage {
    public static class ResponseMessage {
        public static final String STATUS_200 = "Action Success";
        public static final String ENTITY_NOT_FOUND = "Cannot find object";
        public static final String WRONG_PASSWORD = "The password is incorrect. Please try again";
    }
}
