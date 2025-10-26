package com.Zeta.SPLITWISE.EQUALLY.model;

public class AddAmount {
    private Integer amount;
    private String description;

    // Constructors
    public AddAmount() {
    }

    public AddAmount(Integer amount, String description) {
        this.amount = amount;
        this.description = description;
    }

    // Getters
    public Integer getAmount() {
        return amount;
    }

    public String getDescription() {
        return description;
    }

    // Setters
    public void setAmount(Integer amount) {
        this.amount = amount;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}