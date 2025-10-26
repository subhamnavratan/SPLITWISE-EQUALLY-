package com.Zeta.SPLITWISE.EQUALLY.service;

import com.Zeta.SPLITWISE.EQUALLY.model.MemberDetail;
import com.Zeta.SPLITWISE.EQUALLY.model.Members;
import com.Zeta.SPLITWISE.EQUALLY.model.Transaction;
import com.Zeta.SPLITWISE.EQUALLY.model.TransactionDetail;
import com.Zeta.SPLITWISE.EQUALLY.repository.MembersRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class CalculationService {

    @Autowired
    private MembersRepository membersRepository;

    private Members getMembersDoc(String groupName) {
        return membersRepository.findByGroupName(groupName)
                .orElseThrow(() -> new NoSuchElementException("Group not found with name: " + groupName));
    }

    // ----------------------------------------------------------------
    // 1. Basic Calculations (Total, Average)
    // ----------------------------------------------------------------

    public int calculateTotal(String groupName) {
        return getMembersDoc(groupName).getMembers().stream()
                .flatMap(member -> member.getDetail() != null ? member.getDetail().stream() : java.util.stream.Stream.empty())
                .mapToInt(TransactionDetail::getAmount)
                .sum();
    }

    public double calculateAverage(String groupName) {
        List<MemberDetail> members = getMembersDoc(groupName).getMembers();
        if (members.isEmpty()) return 0.0;

        int totalAmount = calculateTotal(groupName);
        return (double) totalAmount / members.size();
    }

    // ----------------------------------------------------------------
    // 2. Net Balance Calculation
    // ----------------------------------------------------------------

    /**
     * Calculates the net balance (Paid - Share).
     * The output List has the final debt/credit status, but the database storage is preserved.
     */
    public List<MemberDetail> getMemberDetailsWithNetBalance(String groupName) {
        Members membersDoc = getMembersDoc(groupName);
        double averageShare = calculateAverage(groupName);

        // Map to store calculated debt/credit temporarily: UserId -> NetDebtCredit
        Map<String, Integer> calculatedNetBalances = new HashMap<>();

        List<MemberDetail> details = membersDoc.getMembers().stream()
                .peek(member -> {
                    // 1. Calculate the true paid amount from the transactions.
                    int actualPaidSum = member.getDetail() != null ?
                            member.getDetail().stream().mapToInt(TransactionDetail::getAmount).sum() : 0;

                    // 2. Calculate Net Debt/Credit.
                    int netDebtCredit = (int) Math.round(actualPaidSum - averageShare);

                    // 3. Store the calculated debt/credit amount for the next step (Settlement).
                    calculatedNetBalances.put(member.getUserId(), netDebtCredit);

                    // 4. IMPORTANT: Restore the field to the true Total Paid amount for database integrity.
                    member.setTotalPaidAmount(actualPaidSum);
                })
                .collect(Collectors.toList());

        // Save the member details, ensuring totalPaidAmount contains the actual sum paid.
        membersRepository.save(membersDoc);

        // Final modification: Set the calculated debt/credit status for the API response.
        // This is necessary because the calculation method expects the net balance.
        return details.stream()
                .peek(member -> {
                    member.setTotalPaidAmount(calculatedNetBalances.get(member.getUserId()));
                })
                .collect(Collectors.toList());
    }

    // ----------------------------------------------------------------
    // 3. CORE SPLITWISE LOGIC (Debt Simplification)
    // ----------------------------------------------------------------

    public List<Transaction> calculateSimplifiedTransactions(String groupName) {
        // This method relies on getMemberDetailsWithNetBalance returning the calculated debt/credit.
        List<MemberDetail> memberDetails = getMemberDetailsWithNetBalance(groupName);

        // Map names for readable output
        Map<String, String> userIdToNameMap = memberDetails.stream()
                .collect(Collectors.toMap(MemberDetail::getUserId, MemberDetail::getName));

        // Step 2: Map Balances by unique UserId -> Debt/Credit Amount
        // Since getMemberDetailsWithNetBalance now returns the calculated value, this is correct.
        Map<String, Integer> balances = memberDetails.stream()
                .collect(Collectors.toMap(MemberDetail::getUserId, MemberDetail::getTotalPaidAmount));

        // Step 3: Separate and sort Payers and Receivers

        List<Map.Entry<String, Integer>> payers = balances.entrySet().stream()
                .filter(e -> e.getValue() < 0)
                .sorted(Comparator.comparingInt((Map.Entry<String, Integer> e) -> e.getValue()))
                .collect(Collectors.toList());

        List<Map.Entry<String, Integer>> receivers = balances.entrySet().stream()
                .filter(e -> e.getValue() > 0)
                .sorted(Comparator.comparingInt((Map.Entry<String, Integer> e) -> e.getValue()).reversed())
                .collect(Collectors.toList());

        List<Transaction> transactions = new ArrayList<>();
        int i = 0;
        int j = 0;

        // Step 4: Greedy algorithm loop
        while (i < payers.size() && j < receivers.size()) {
            Map.Entry<String, Integer> payerEntry = payers.get(i);
            Map.Entry<String, Integer> receiverEntry = receivers.get(j);

            int debt = -payerEntry.getValue();
            int credit = receiverEntry.getValue();

            int settlementAmount = Math.min(debt, credit);

            if (settlementAmount > 0) {
                transactions.add(new Transaction(
                        userIdToNameMap.get(payerEntry.getKey()),
                        userIdToNameMap.get(receiverEntry.getKey()),
                        settlementAmount
                ));
            }

            int remainingDebt = debt - settlementAmount;
            int remainingCredit = credit - settlementAmount;

            payerEntry.setValue(-remainingDebt);
            receiverEntry.setValue(remainingCredit);

            if (remainingDebt == 0) i++;
            if (remainingCredit == 0) j++;
        }

        return transactions;
    }
}