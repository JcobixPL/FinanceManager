package com.financemanager;

import java.time.LocalDate;

public class Transaction {
    private String type;
    private double amount;
    private String title;
    private String category;
    private LocalDate date;
    private int id;

    public int getId() {
        return id;
    }
    public String getType() {
        return type;
    }
    public double getAmount() {
        return amount;
    }
    public String getTitle() {
        return title;
    }
    public String getCategory() {
        return category;
    }
    public LocalDate getDate() {
        return date;
    }
    public Transaction(String type, double amount, String title, String category, LocalDate date, Integer id) {
        this.type = type;
        this.amount = amount;
        this.title = title;
        this.category = category;
        this.date = date;
        this.id = id;
    }
}

