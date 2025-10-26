package com.Zeta.SPLITWISE.EQUALLY.service;

import com.Zeta.SPLITWISE.EQUALLY.model.*;
import com.Zeta.SPLITWISE.EQUALLY.DTO.CreateGroupRequest; // Not used here, but kept for context
import com.Zeta.SPLITWISE.EQUALLY.repository.GroupRepository;
import com.Zeta.SPLITWISE.EQUALLY.repository.MembersRepository;
import com.Zeta.SPLITWISE.EQUALLY.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

@Service
public class MembersService {

    @Autowired private MembersRepository membersRepository;
    @Autowired private GroupRepository groupRepository;
    @Autowired private UserRepository userRepository;

    // =================================================================
    // PUBLIC API METHODS (Using Phone)
    // =================================================================

    public String addPaymentByPhone(String groupName, Long phone, AddAmount addAmount) {
        User user = userRepository.findByPhone(phone)
                .orElseThrow(() -> new NoSuchElementException("User not found with phone number: " + phone));

        String userId = user.getUserId();

        TransactionDetail transaction = new TransactionDetail(
                addAmount.getAmount(),
                addAmount.getDescription()
        );

        addPaymentToMember(groupName, userId, transaction);
        return userId;
    }

    public List<MemberDetail> getMembersByGroup(String groupName) {
        Optional<Members> optionalMembers = membersRepository.findByGroupName(groupName);
        return optionalMembers.map(Members::getMembers).orElse(List.of());
    }

    // =================================================================
    // CORE LOGIC METHODS
    // =================================================================

    /**
     * Core internal method to update a member’s transaction history.
     * Updates the totalPaidAmount field to reflect the running sum.
     */
    public Members addPaymentToMember(String groupName, String userId, TransactionDetail transaction) {
        // Step 1: Check if group exists
        Group group = groupRepository.findByGroupName(groupName)
                .orElseThrow(() -> new NoSuchElementException("Group not found: " + groupName));

        // Step 2: Validate userId is part of GROUP
        if (!group.getMemberUserIds().contains(userId)) {
            throw new IllegalArgumentException("User " + userId + " is not a member of group " + groupName);
        }

        // Step 3: Fetch MEMBERS document
        Members membersDoc = membersRepository.findByGroupName(groupName)
                .orElseThrow(() -> new NoSuchElementException("Members document not found for group: " + groupName));

        // Step 4: Find matching member
        MemberDetail member = membersDoc.getMembers().stream()
                .filter(m -> m.getUserId().equals(userId))
                .findFirst()
                .orElseThrow(() -> new NoSuchElementException("Member data not found within MEMBERS document."));

        // Step 5: Add transaction and update totalPaidAmount
        if (member.getDetail() == null) {
            member.setDetail(new ArrayList<>());
        }
        member.getDetail().add(transaction);

        // **FIXED LINE:** Use the new explicit field name
        member.setTotalPaidAmount(member.getTotalPaidAmount() + transaction.getAmount());

        // Step 6: Save updated MEMBERS doc
        return membersRepository.save(membersDoc);
    }
}