package server;

import common.Account;
import common.Transaction;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;

public class BankData implements Serializable {

    public HashMap<Integer, Account> accounts;
    public HashMap<Integer, List<Transaction>> transactions;
    public int accountCounter;

    public BankData(HashMap<Integer, Account> accounts,
                    HashMap<Integer, List<Transaction>> transactions,
                    int accountCounter) {

        this.accounts = accounts;
        this.transactions = transactions;
        this.accountCounter = accountCounter;
    }
}