package com.socialmedia.clover_network.enumuration;

public enum UserStatus {
    INACTIVE(0, "Inactive"),
    ACTIVE(1,"Active"),
    DELETED(2, "Deleted"),
    OTHER(10,"Other"),;

    int statusCode;
    String statusName;

    UserStatus(int statusCode, String statusName) {
        this.statusCode = statusCode;
        this.statusName = statusName;
    }

    public String getStatusName() {
        return statusName;
    }

    public UserStatus getStatusByCode(int statusCode) {
        for (UserStatus status : UserStatus.values()) {
            if (statusCode == status.statusCode)
                return status;
        }
        return null;
    }

}
