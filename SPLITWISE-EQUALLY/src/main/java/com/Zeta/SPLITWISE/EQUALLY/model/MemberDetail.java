package com.Zeta.SPLITWISE.EQUALLY.model;

import java.util.ArrayList;
import java.util.List;

public class MemberDetail {
    private String userId;
    private String name;

    // RENAMED: This field explicitly tracks the running sum of payments.
    private Integer totalPaidAmount = 0;

    private List<TransactionDetail> detail; // Transaction history

    // =================================================================
    // CONSTRUCTORS
    // =================================================================

    // 1. No-Argument Constructor (Required by Spring/MongoDB)
    public MemberDetail() {
        this.detail = new ArrayList<>();
        this.totalPaidAmount = 0;
    }

    // 2. All-Argument Constructor
    public MemberDetail(String userId, String name, Integer totalPaidAmount, List<TransactionDetail> detail) {
        this.userId = userId;
        this.name = name;
        this.totalPaidAmount = totalPaidAmount;
        this.detail = detail;
    }

    // 3. Simple Constructor (Used in GroupService when adding a new member)
    public MemberDetail(String userId, String name) {
        this.userId = userId;
        this.name = name;
        this.totalPaidAmount = 0;
        this.detail = new ArrayList<>();
    }


    // =================================================================
    // GETTERS AND SETTERS
    // =================================================================

    // totalPaidAmount Getter and Setter (The main change)
    public Integer getTotalPaidAmount() {
        return totalPaidAmount;
    }

    public void setTotalPaidAmount(Integer totalPaidAmount) {
        this.totalPaidAmount = totalPaidAmount;
    }

    // userId Getter and Setter
    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    // name Getter and Setter
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    // detail Getter and Setter (Fixes the "Cannot resolve method 'getDetail'" error)
    public List<TransactionDetail> getDetail() {
        return detail;
    }

    public void setDetail(List<TransactionDetail> detail) {
        this.detail = detail;
    }
}