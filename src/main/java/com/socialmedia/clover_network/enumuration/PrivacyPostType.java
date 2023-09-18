package com.socialmedia.clover_network.enumuration;

public enum PrivacyPostType {
    PUBLIC("PUBLIC"),
    FRIEND("FRIEND"),
    ONLY_ME("ONLY_ME"),
    LIMITED_GROUP("LIMITED_GROUP");

    final String type;

    PrivacyPostType(String type) {
        this.type = type;
    }
}
