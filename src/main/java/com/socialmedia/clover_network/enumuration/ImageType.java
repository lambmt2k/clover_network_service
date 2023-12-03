package com.socialmedia.clover_network.enumuration;

public enum ImageType {
    NONE(0),
    USER_AVATAR(1),
    USER_BANNER(2),
    FEED_IMAGES(3);

    final int type;

    ImageType(int type) {
        this.type = type;
    }
}
