package com.Zeta.SPLITWISE.EQUALLY.controller;

import com.Zeta.SPLITWISE.EQUALLY.model.AddAmount;
import com.Zeta.SPLITWISE.EQUALLY.model.MemberDetail;
import com.Zeta.SPLITWISE.EQUALLY.model.TransactionDetail;
import com.Zeta.SPLITWISE.EQUALLY.service.MembersService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/members")
public class MembersController {

    @Autowired
    private MembersService membersService;

    // GET /members/{groupName}
    @GetMapping("/{groupName}")
    public ResponseEntity<List<MemberDetail>> getMembers(@PathVariable String groupName) {
        List<MemberDetail> members = membersService.getMembersByGroup(groupName);
        if (members.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(members);
    }

    @PostMapping("/paymentByPhone/{groupName}/{phone}")
    public ResponseEntity<String> addPaymentByPhone(@PathVariable String groupName,
                                                    @PathVariable Long phone, // Changed from String userId to Long phone
                                                    @RequestBody AddAmount addAmount) {
        try {
            // Service handles the phone lookup and transaction addition
            String userId = membersService.addPaymentByPhone(groupName, phone, addAmount);

            return ResponseEntity.ok("Payment added successfully for user (Phone: " + phone + ")");
        } catch (Exception e) {
            // Use specific status codes if possible (e.g., 404 for User Not Found)
            return ResponseEntity.badRequest().body("Payment failed: " + e.getMessage());
        }
    }
}