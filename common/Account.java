package common;

import java.io.Serializable;
import java.util.List;import java.util.ArrayList;
import java.util.List;



public class Account implements Serializable {
private String role; // "ADMIN" or "USER"
    private int accountNumber;
    private String name;
    private String pin;
    private double balance;
     private List<Transaction> history = new ArrayList<>();

 public Account(int accNo, String name, String pin, double balance, String role) {
    
    this.name = name;
    this.pin = pin;
    this.balance = balance;
    this.role = role;
}

    // Getters
    public int getAccountNumber() { return accountNumber; }
    public String getName() { return name; }
    public String getPin() { return pin; }
   
    public String getRole() {
    return role;
}

    // Setters
    public double getBalance() {
    return balance;
}

public void setBalance(double balance) {
    this.balance = balance;
}

public List<Transaction> getHistory() {
    return history;
}
    
}