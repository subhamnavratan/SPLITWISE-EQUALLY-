package com.Zeta.SPLITWISE.EQUALLY.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.util.List;

@Document(collection="_MEMBERS")
public class Members {
    @Id
    private String id;
    private String groupName;
    private List<MemberDetail> members;

    // Constructors
    public Members() {
    }

    public Members(String id, String groupName, List<MemberDetail> members) {
        this.id = id;
        this.groupName = groupName;
        this.members = members;
    }

    // Getters
    public String getId() {
        return id;
    }

    public String getGroupName() {
        return groupName;
    }

    public List<MemberDetail> getMembers() {
        return members;
    }

    // Setters
    public void setId(String id) {
        this.id = id;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public void setMembers(List<MemberDetail> members) {
        this.members = members;
    }
}