package com.socialmedia.clover_network.enumuration;

public enum Gender {
    MALE(0),
    FEMALE(1),
    OTHER(2);

    final int gender;

    Gender(int gender) {
        this.gender = gender;
    }
}
