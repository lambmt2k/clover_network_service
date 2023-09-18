package com.socialmedia.clover_network.enumuration;

public enum GroupMemberRole {
    OWNER(0, "OWNER"),
    ADMIN(1,"ADMIN"),
    MEMBER(2,"MEMBER"),;

    int role;
    String roleName;

    GroupMemberRole(int role, String roleName) {
        this.role = role;
        this.roleName = roleName;
    }

    public String getRoleName() {
        return roleName;
    }

    public GroupMemberRole getRoleByCode(int roleCode) {
        for (GroupMemberRole role : GroupMemberRole.values()) {
            if (roleCode == role.role)
                return role;
        }
        return null;
    }
}
