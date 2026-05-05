package common;

import java.io.Serializable;

public class Transaction implements Serializable {

    private String type;        // Deposit / Withdraw / Transfer
    private double amount;
    private String time;
    private String details;

    public Transaction(String type, double amount, String time, String details) {
        this.type = type;
        this.amount = amount;
        this.time = time;
        this.details = details;
    }

    // Getters
    public String getType() { return type; }
    public double getAmount() { return amount; }
    public String getTime() { return time; }
    public String getDetails() { return details; }

    @Override
    public String toString() {
        return "[" + time + "] " + type +
                " | Amount: " + amount +
                " | " + details;
    }
}