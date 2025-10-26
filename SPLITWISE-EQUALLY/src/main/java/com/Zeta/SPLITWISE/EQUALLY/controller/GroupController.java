package com.Zeta.SPLITWISE.EQUALLY.controller;

import com.Zeta.SPLITWISE.EQUALLY.DTO.CreateGroupRequest;
import com.Zeta.SPLITWISE.EQUALLY.model.Group;
import com.Zeta.SPLITWISE.EQUALLY.service.GroupService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/groups")
public class GroupController {

    private final GroupService groupService;

    @Autowired
    public GroupController(GroupService groupService) {
        this.groupService = groupService;
    }

    @PostMapping("/createByPhone") // <--- This is the exact path segment that was missing
    public ResponseEntity<Group> createGroupByPhone(@RequestBody CreateGroupRequest request) {
        try {
            // Call the modified service method
            Group saved = groupService.createGroupByPhone(request.getGroupName(), request.getCreatorPhone());
            return ResponseEntity.ok(saved);
        } catch (Exception e) {
            // The 400 Bad Request error handler is sufficient here
            return ResponseEntity.badRequest().body(null);
        }
    }

    // =================================================================
    // GET GROUPS BY PHONE NUMBER (New API)
    // =================================================================
    @GetMapping("/phone/{phone}")
    public ResponseEntity<List<Group>> getGroupsByPhone(@PathVariable Long phone) {
        try {
            // Service handles the lookup: phone -> userId -> groups
            List<Group> groups = groupService.getGroupsByPhone(phone);
            return ResponseEntity.ok(groups);
        } catch (NoSuchElementException e) {
            // Handle case where phone number doesn't exist
            return ResponseEntity.notFound().build();
        }
    }
    // =================================================================
    // ADD MEMBER BY PHONE NUMBER (New API)
    // =================================================================
    @PostMapping("/addByPhone/{groupName}/{phone}")
    public ResponseEntity<Group> addMemberByPhone(@PathVariable String groupName, @PathVariable Long phone) {
        try {
            // Service handles the lookup: phone -> userId -> adds member
            Group updated = groupService.addMemberByPhone(groupName, phone);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    // =================================================================
    // REMOVE MEMBER BY PHONE NUMBER (New API)
    // =================================================================
    @DeleteMapping("/removeByPhone/{groupName}/{phone}")
    public ResponseEntity<Group> removeMemberByPhone(@PathVariable String groupName, @PathVariable Long phone) {
        try {
            // Service handles the lookup: phone -> userId -> removes member
            Group updated = groupService.removeMemberByPhone(groupName, phone);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    @DeleteMapping("/delete/{groupName}")
    public ResponseEntity<String> deleteGroup(@PathVariable String groupName) {
        try {
            groupService.deleteGroup(groupName);
            return ResponseEntity.ok("Group deleted successfully.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error deleting group: " + e.getMessage());
        }
    }
}