package com.socialmedia.clover_network.enumuration;

public enum UserRole {
    ADMIN(0, "ADMIN"),
    USER(1,"USER"),
    OTHER(10,"OTHER"),;

    int role;
    String roleName;

    UserRole(int role, String roleName) {
        this.role = role;
        this.roleName = roleName;
    }

    public String getRoleName() {
        return roleName;
    }

    public UserRole getRoleByCode(int roleCode) {
        for (UserRole role : UserRole.values()) {
            if (roleCode == role.role)
                return role;
        }
        return null;
    }
}
