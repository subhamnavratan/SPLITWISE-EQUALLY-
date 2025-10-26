package com.Zeta.SPLITWISE.EQUALLY.DTO;

public class CreateGroupRequest {
    private String groupName;
    private Long creatorPhone; // Used for lookup

    // Getters and Setters...
    public String getGroupName() { return groupName; }
    public void setGroupName(String groupName) { this.groupName = groupName; }
    public Long getCreatorPhone() { return creatorPhone; }
    public void setCreatorPhone(Long creatorPhone) { this.creatorPhone = creatorPhone; }
}
