package com.Zeta.SPLITWISE.EQUALLY.service;

import com.Zeta.SPLITWISE.EQUALLY.model.*;
import com.Zeta.SPLITWISE.EQUALLY.DTO.CreateGroupRequest;
import com.Zeta.SPLITWISE.EQUALLY.repository.GroupRepository;
import com.Zeta.SPLITWISE.EQUALLY.repository.MembersRepository;
import com.Zeta.SPLITWISE.EQUALLY.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.*;

@Service
public class GroupService {

    private final GroupRepository groupRepository;
    private final UserRepository userRepository;
    private final MembersRepository membersRepository;

    @Autowired
    public GroupService(GroupRepository groupRepository, UserRepository userRepository, MembersRepository membersRepository) {
        this.groupRepository = groupRepository;
        this.userRepository = userRepository;
        this.membersRepository = membersRepository;
    }

    /**
     * Helper method to look up and return the User object using the phone number.
     * Throws NoSuchElementException if the user is not found.
     */
    private User getUserByPhone(Long phone) {
        return userRepository.findByPhone(phone)
                .orElseThrow(() -> new NoSuchElementException("User not found with phone number: " + phone));
    }

    /**
     * Core internal logic for saving the Group and creating the MEMBERS document.
     */
    private Group saveNewGroupAndMemberDoc(Group group, User creator) {
        // 1. Save group
        Group savedGroup = groupRepository.save(group);

        // 2. Prepare and save MEMBERS document
        // MemberDetail constructor: (userId, name, netBalance, detail)
        MemberDetail creatorDetail = new MemberDetail(creator.getUserId(), creator.getName(), 0, new ArrayList<>());

        // Members constructor: (id, groupName, members)
        Members members = new Members(null, group.getGroupName(), new ArrayList<>(List.of(creatorDetail)));
        membersRepository.save(members);

        return savedGroup;
    }

    // =================================================================
    // PRIMARY API METHODS (Use Phone for External Requests)
    // =================================================================

    /**
     * Creates a new group by looking up the creator's userId based on their phone number.
     * Maps the phone to userId internally before saving.
     */
    public Group createGroupByPhone(String groupName, Long creatorPhone) {
        // 1. Validation
        if (groupRepository.findByGroupName(groupName).isPresent()) {
            throw new IllegalArgumentException("Group with this name already exists.");
        }

        // 2. Lookup User by Phone
        User user = getUserByPhone(creatorPhone);

        String creatorId = user.getUserId();

        // 3. Construct the Group model object with the userId
        Group newGroup = new Group();
        newGroup.setGroupName(groupName);
        newGroup.setMemberUserIds(new ArrayList<>(List.of(creatorId)));

        // 4. Delegate to core saving logic
        return saveNewGroupAndMemberDoc(newGroup, user);
    }

    /**
     * Fetches all groups a user belongs to, using the mobile number for lookup.
     */
    public List<Group> getGroupsByPhone(Long phone) {
        // Find the user ID from the phone
        String userId = getUserByPhone(phone).getUserId();

        // Call existing core method
        return getGroupsByUserId(userId);
    }

    /**
     * Adds a member to a group, using the mobile number for member identification.
     */
    public Group addMemberByPhone(String groupName, Long phone) {
        // Find the user ID from the phone
        String userId = getUserByPhone(phone).getUserId();

        // Call existing core method
        return addMemberByUserId(groupName, userId);
    }

    /**
     * Removes a member from a group, using the mobile number for member identification.
     */
    public Group removeMemberByPhone(String groupName, Long phone) {
        // Find the user ID from the phone
        String userId = getUserByPhone(phone).getUserId();

        // Call existing core method
        return removeMemberByUserId(groupName, userId);
    }

    // =================================================================
    // CORE LOGIC METHODS (Private/Internal - Always use userId)
    // =================================================================

    // Kept as internal core logic
    public Group createGroup(Group group) {
        if (groupRepository.findByGroupName(group.getGroupName()).isPresent()) {
            throw new IllegalArgumentException("Group with this name already exists.");
        }

        List<String> userIds = group.getMemberUserIds();
        if (userIds == null || userIds.isEmpty()) {
            throw new IllegalArgumentException("At least one member (creator) must be specified by userId.");
        }

        String creatorId = userIds.get(0);
        User user = userRepository.findById(creatorId)
                .orElseThrow(() -> new NoSuchElementException("Creator not found with userId: " + creatorId));

        return saveNewGroupAndMemberDoc(group, user);
    }

    public List<Group> getGroupsByUserId(String userId) {
        return groupRepository.findByMemberUserIdsContaining(userId);
    }

    public void deleteGroup(String groupName) {
        Group group = groupRepository.findByGroupName(groupName)
                .orElseThrow(() -> new NoSuchElementException("Group not found: " + groupName));

        groupRepository.delete(group);
        membersRepository.findByGroupName(groupName).ifPresent(membersRepository::delete);
    }

    public Group addMemberByUserId(String groupName, String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("User not found with userId: " + userId));

        Group group = groupRepository.findByGroupName(groupName)
                .orElseThrow(() -> new NoSuchElementException("Group not found: " + groupName));

        // 1. Add userId to GROUP collection
        if (!group.getMemberUserIds().contains(userId)) {
            group.getMemberUserIds().add(userId);
            groupRepository.save(group);
        }

        // 2. Add member to MEMBERS collection
        Members membersDoc = membersRepository.findByGroupName(groupName)
                .orElse(new Members(null, groupName, new ArrayList<>()));

        boolean alreadyExists = membersDoc.getMembers().stream()
                .anyMatch(m -> m.getUserId().equals(userId));

        if (!alreadyExists) {
            MemberDetail newMember = new MemberDetail(userId, user.getName(), 0, new ArrayList<>());
            membersDoc.getMembers().add(newMember);
            membersRepository.save(membersDoc);
        }

        return group;
    }

    public Group removeMemberByUserId(String groupName, String userId) {
        Group group = groupRepository.findByGroupName(groupName)
                .orElseThrow(() -> new NoSuchElementException("Group not found: " + groupName));

        // 1. Remove from GROUP memberUserIds list
        group.getMemberUserIds().remove(userId);
        groupRepository.save(group);

        // 2. Remove from MEMBERS collection
        Members membersDoc = membersRepository.findByGroupName(groupName)
                .orElseThrow(() -> new NoSuchElementException("Members document not found for group: " + groupName));

        membersDoc.getMembers().removeIf(m -> m.getUserId().equals(userId));
        membersRepository.save(membersDoc);

        return group;
    }
}