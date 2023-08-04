package com.socialmedia.clover_network.enumuration;

public enum UserStatus {
    ACTIVE("active"),
    INACTIVE("inactive"),
    DELETED("deleted"),
    OTHER("other"),
    ;

    private final String code;

    UserStatus(String code) { this.code = code; }

    public String getCode() { return code; }

    public UserStatus getStatusByCode(String code) {
        for (UserStatus status : UserStatus.values()) {
            if (code.equalsIgnoreCase(status.code))
                return status;
        }
        return null;
    }

}
