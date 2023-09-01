package com.socialmedia.clover_network.enumuration;

public enum AccountType {
    EMAIL("EMAIL"),
    GOOGLE("GOOGLE"),
    OTHER("OTHER");

    final String type;

    AccountType(String type) {
        this.type = type;
    }
}
