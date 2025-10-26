package com.Zeta.SPLITWISE.EQUALLY.model;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.util.List;

@Document(collection = "_GROUPS")
public class Group {
    @Id
    private String id;

    private String groupName;
    private List<String> memberUserIds;

    // Constructors
    public Group() {
    }

    public Group(String id, String groupName, List<String> memberUserIds) {
        this.id = id;
        this.groupName = groupName;
        this.memberUserIds = memberUserIds;
    }

    // Getters
    public String getId() {
        return id;
    }

    public String getGroupName() {
        return groupName;
    }

    public List<String> getMemberUserIds() {
        return memberUserIds;
    }

    // Setters
    public void setId(String id) {
        this.id = id;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public void setMemberUserIds(List<String> memberUserIds) {
        this.memberUserIds = memberUserIds;
    }
}