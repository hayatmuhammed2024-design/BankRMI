package client;

import common.BankRemote;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Scanner;

public class BankClient {

    public static void main(String[] args) {

        try {
            Registry registry = LocateRegistry.getRegistry("localhost", 1099);
            BankRemote bank = (BankRemote) registry.lookup("BankService");

            Scanner sc = new Scanner(System.in);

            System.out.print("Account Number: ");
            int accNo = sc.nextInt();

            System.out.print("PIN: ");
            String pin = sc.next();

            // 🔐 LOGIN
            if (!bank.login(accNo, pin)) {
                System.out.println("❌ Login failed");
                return;
            }

            String role = bank.getRole(accNo);

            // ================= ADMIN =================
            if (role.equals("ADMIN")) {

                System.out.println("✅ Welcome Admin");

                while (true) {
                    System.out.println("\n--- ADMIN MENU ---");
                    System.out.println("1. Create Account");
                    System.out.println("2. Exit");

                    int op = sc.nextInt();

                    if (op == 1) {

                        System.out.print("Name: ");
                        String name = sc.next();

                        System.out.print("PIN: ");
                        String newPin = sc.next();

                        int acc = bank.createAccount(name, newPin, true);

                        if (acc == -2) {
                            System.out.println("⚠ Account already exists!");
                        } else if (acc == -3) {
                            System.out.println("⚠ Max 3 accounts reached!");
                        } else if (acc == -1) {
                            System.out.println("❌ Not allowed");
                        } else {
                            System.out.println("✅ Account created: " + acc);
                        }
                    }

                    else if (op == 2) {
                        bank.logout(accNo);
                        System.out.println("Logged out");
                        break;
                    }
                }
            }

            // ================= USER =================
            else if (role.equals("USER")) {

                System.out.println("✅ Welcome User");

                while (true) {
                    System.out.println("\n--- USER MENU ---");
                    System.out.println("1. Balance");
                    System.out.println("2. Deposit");
                    System.out.println("3. Withdraw");
                    System.out.println("4. Transfer");
                    System.out.println("5. History");
                    System.out.println("6. Exit");

                    int op = sc.nextInt();

                    if (op == 1) {
                        System.out.println("Balance: " + bank.getBalance(accNo));
                    }

                    else if (op == 2) {
                        System.out.print("Amount: ");
                        double amount = sc.nextDouble();

                        System.out.println(bank.deposit(accNo, amount));
                    }

                    else if (op == 3) {
                        System.out.print("Amount: ");
                        double amount = sc.nextDouble();

                        System.out.println(bank.withdraw(accNo, amount));
                    }

                    else if (op == 4) {
                        System.out.print("To Account: ");
                        int to = sc.nextInt();

                        System.out.print("Amount: ");
                        double amount = sc.nextDouble();

                        String success = bank.transfer(accNo, to, amount);
                        System.out.println(success);
                    }

                    else if (op == 5) {
                        System.out.println("---- HISTORY ----");
                        for (String h : bank.getTransactionHistory(accNo)) {
                            System.out.println(h);
                        }
                    }

                    else if (op == 6) {
                        bank.logout(accNo);
                        System.out.println("Logged out");
                        break;
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}