package com.Zeta.SPLITWISE.EQUALLY.controller;

import com.Zeta.SPLITWISE.EQUALLY.model.MemberDetail;
import com.Zeta.SPLITWISE.EQUALLY.model.Transaction;
import com.Zeta.SPLITWISE.EQUALLY.service.CalculationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/calculate")
public class CalculationController {

    @Autowired
    private CalculationService calculationService;

    // ------------------------------------------------------------------------
    // Total Calculation Endpoint
    // ------------------------------------------------------------------------
    @GetMapping("/total/{groupName}")
    public ResponseEntity<Integer> getTotal(@PathVariable String groupName) {
        try {
            int total = calculationService.calculateTotal(groupName);
            return ResponseEntity.ok(total);
        } catch (NoSuchElementException e) {
            // IMPROVEMENT: Use 404 NOT FOUND status code when the resource (group) is missing.
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            // Catch other issues (like an unexpected server problem) as 500 or 400
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // ------------------------------------------------------------------------
    // Member Balances Endpoint
    // ------------------------------------------------------------------------
    @GetMapping("/balances/{groupName}")
    public ResponseEntity<List<MemberDetail>> getMemberBalances(@PathVariable String groupName) {
        try {
            List<MemberDetail> details = calculationService.getMemberDetailsWithNetBalance(groupName);
            return ResponseEntity.ok(details);
        } catch (NoSuchElementException e) {
            // IMPROVEMENT: Use 404 NOT FOUND status code when the resource (group) is missing.
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            // Catch other issues
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // ------------------------------------------------------------------------
    // Debt Settlement (Splitwise Core) Endpoint
    // ------------------------------------------------------------------------
    /**
     * **The Main Splitwise Feature**
     * GET /calculate/settle/{groupName}
     * Returns the minimum set of payments required to clear all debts.
     */
    @GetMapping("/settle/{groupName}")
    public ResponseEntity<List<Transaction>> getSimplifiedSettlements(@PathVariable String groupName) {
        try {
            List<Transaction> transactions = calculationService.calculateSimplifiedTransactions(groupName);
            return ResponseEntity.ok(transactions);

        } catch (NoSuchElementException e) {
            // Handling for Group Not Found is excellent.
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();

        } catch (Exception e) {
            // Handling for general computation error (e.g., divide by zero, inconsistent data).
            // Return 400 Bad Request and include the error message using the Transaction model for simplicity.
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(List.of(
                    new Transaction("System Error", e.getMessage(), 0)
            ));
        }
    }
}