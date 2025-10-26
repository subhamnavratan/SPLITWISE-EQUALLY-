package com.Zeta.SPLITWISE.EQUALLY.model;

/**
 * Represents a final, simplified payment required to settle the group debt.
 */
public class Transaction {
    private String payerName;
    private String receiverName;
    private int amount;

    // Constructors
    public Transaction() {
    }

    public Transaction(String payerName, String receiverName, int amount) {
        this.payerName = payerName;
        this.receiverName = receiverName;
        this.amount = amount;
    }

    // Getters
    public String getPayerName() {
        return payerName;
    }

    public String getReceiverName() {
        return receiverName;
    }

    public int getAmount() {
        return amount;
    }

    // Setters (less common for a final output model, but included for completeness)
    public void setPayerName(String payerName) {
        this.payerName = payerName;
    }

    public void setReceiverName(String receiverName) {
        this.receiverName = receiverName;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    @Override
    public String toString() {
        return payerName + " owes " + receiverName + ": $" + amount;
    }
}