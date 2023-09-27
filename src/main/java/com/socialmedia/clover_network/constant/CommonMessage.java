package com.socialmedia.clover_network.constant;

import org.springframework.stereotype.Component;

@Component
public class CommonMessage {
    public static class ResponseMessage {
        public static final String ACTION_SUCCESS = "Action Success";
        public static final String ENTITY_NOT_FOUND = "Cannot find object";
        public static final String WRONG_PASSWORD = "The password is incorrect. Please try again";
        public static final String NO_DATA = "There is no data response";
    }
}
