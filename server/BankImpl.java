package server;

import common.Account;
import common.BankRemote;
import common.Transaction;

import java.io.*;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class BankImpl extends UnicastRemoteObject implements BankRemote {

    private HashMap<Integer, Long> lastActive = new HashMap<>();
    private HashMap<Integer, Integer> loginAttempts = new HashMap<>();

    private HashMap<Integer, Account> accounts = new HashMap<>();
    private HashMap<Integer, List<Transaction>> transactions = new HashMap<>();
    private HashMap<Integer, Boolean> loggedIn = new HashMap<>();

    private int accountCounter = 1003;

    private final String ADMIN_USERNAME = "admin";
    private final String ADMIN_PASSWORD = "admin123";

    private static final DateTimeFormatter formatter =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public BankImpl() throws RemoteException {
        super();
        loadData();

        // default admin
        accounts.putIfAbsent(1000,
                new Account(1000, "admin", hash("admin123"), 0, "ADMIN"));

        transactions.putIfAbsent(1000, new ArrayList<>());
        loggedIn.putIfAbsent(1000, false);
    }

    // ================= SECURITY =================
    private String hash(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] bytes = md.digest(input.getBytes());

            StringBuilder sb = new StringBuilder();
            for (byte b : bytes) sb.append(String.format("%02x", b));

            return sb.toString();
        } catch (Exception e) {
            return null;
        }
    }

    private String time() {
        return LocalDateTime.now().format(formatter);
    }

    private boolean isSessionActive(int acc) {
        long now = System.currentTimeMillis();
        long last = lastActive.getOrDefault(acc, 0L);

        if (now - last > 10 * 60 * 1000) { // 10 min
            loggedIn.put(acc, false);
            return false;
        }

        lastActive.put(acc, now);
        return true;
    }

    // ================= LOGIN =================
    @Override
    public boolean login(int accNo, String pin) {

        Account acc = accounts.get(accNo);
        if (acc == null) return false;

        int attempts = loginAttempts.getOrDefault(accNo, 0);
        if (attempts >= 3) return false;

        if (acc.getPin().equals(hash(pin))) {
            loggedIn.put(accNo, true);
            lastActive.put(accNo, System.currentTimeMillis());
            loginAttempts.put(accNo, 0);
            return true;
        }

        loginAttempts.put(accNo, attempts + 1);
        return false;
    }

    // ================= ROLE =================
    @Override
    public String getRole(int accNo) {
        Account acc = accounts.get(accNo);
        return acc != null ? acc.getRole() : "INVALID";
    }

    // ================= LOGOUT =================
    @Override
    public boolean logout(int accNo) {
        loggedIn.put(accNo, false);
        return true;
    }

    // ================= ADMIN LOGIN =================
    @Override
    public boolean adminLogin(String user, String pass) {
        return ADMIN_USERNAME.equals(user) && ADMIN_PASSWORD.equals(pass);
    }

    // ================= BALANCE =================
    @Override
    public double getBalance(int accNo) {

        if (!isSessionActive(accNo)) return -1;
        if (!loggedIn.getOrDefault(accNo, false)) return -1;

        Account acc = accounts.get(accNo);
        return acc != null ? acc.getBalance() : -1;
    }

    // ================= DEPOSIT =================
    @Override
    public synchronized String deposit(int accNo, double amount) {

        if (!isSessionActive(accNo))
            return "❌ Session expired";

        Account acc = accounts.get(accNo);
        if (acc == null) return "❌ Account not found";
        if (amount <= 0) return "❌ Invalid amount";

        acc.setBalance(acc.getBalance() + amount);

        transactions
                .computeIfAbsent(accNo, k -> new ArrayList<>())
                .add(new Transaction("DEPOSIT", amount, time(),
                        "Deposit"));

        saveData();
        return "✅ Deposit successful. Balance: " + acc.getBalance();
    }

    // ================= WITHDRAW =================
    @Override
    public synchronized String withdraw(int accNo, double amount) {

        if (!isSessionActive(accNo))
            return "❌ Session expired";

        Account acc = accounts.get(accNo);
        if (acc == null) return "❌ Account not found";
        if (amount <= 0) return "❌ Invalid amount";
        if (acc.getBalance() < amount) return "❌ Insufficient balance";

        acc.setBalance(acc.getBalance() - amount);

        transactions
                .computeIfAbsent(accNo, k -> new ArrayList<>())
                .add(new Transaction("WITHDRAW", amount, time(),
                        "Withdraw"));

        saveData();
        return "✅ Withdraw successful. Balance: " + acc.getBalance();
    }

    // ================= TRANSFER =================
    @Override
    public synchronized String transfer(int from, int to, double amount) {

        if (!isSessionActive(from))
            return "❌ Session expired";

        Account sender = accounts.get(from);
        Account receiver = accounts.get(to);

        if (sender == null || receiver == null)
            return "❌ Account not found";

        if (amount <= 0)
            return "❌ Invalid amount";

        if (sender.getBalance() < amount)
            return "❌ Insufficient balance";

        sender.setBalance(sender.getBalance() - amount);
        receiver.setBalance(receiver.getBalance() + amount);

        transactions.computeIfAbsent(from, k -> new ArrayList<>())
                .add(new Transaction("TRANSFER OUT", amount, time(),
                        "To " + to));

        transactions.computeIfAbsent(to, k -> new ArrayList<>())
                .add(new Transaction("TRANSFER IN", amount, time(),
                        "From " + from));

        saveData();

        return "✅ Transfer successful. Balance: " + sender.getBalance();
    }

    // ================= HISTORY =================
    @Override
    public List<String> getTransactionHistory(int accNo) {

        List<Transaction> list = transactions.getOrDefault(accNo, new ArrayList<>());
        List<String> result = new ArrayList<>();

        for (Transaction t : list) {
            result.add(t.toString());
        }

        return result;
    }

    // ================= CREATE ACCOUNT =================
    @Override
    public int createAccount(String name, String pin, boolean isAdmin) {

        if (!isAdmin) return -1;

        // FIXED: only name check (no PIN check)
        for (Account a : accounts.values()) {
            if (a.getName().equalsIgnoreCase(name)) {
                return -2;
            }
        }

        int accNo = accountCounter++;

        accounts.put(accNo,
                new Account(accNo, name, hash(pin), 0, "USER"));

        transactions.put(accNo, new ArrayList<>());
        loggedIn.put(accNo, false);

        saveData();
        return accNo;
    }

    // ================= SAVE =================
    private void saveData() {
        try (ObjectOutputStream out =
                     new ObjectOutputStream(new FileOutputStream("bank.dat"))) {

            BankData data = new BankData(accounts, transactions, accountCounter);
            out.writeObject(data);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ================= LOAD =================
    private void loadData() {
        try (ObjectInputStream in =
                     new ObjectInputStream(new FileInputStream("bank.dat"))) {

            BankData data = (BankData) in.readObject();

            accounts = data.accounts;
            transactions = data.transactions;
            accountCounter = data.accountCounter;

        } catch (Exception e) {
            System.out.println("No previous data found.");
        }
    }
}