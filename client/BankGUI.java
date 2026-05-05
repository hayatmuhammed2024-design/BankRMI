package client;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import common.BankRemote;

public class BankGUI extends Application {

    BankRemote bank;
    Stage window;

    Scene loginScene, adminScene, userScene;

    int currentAcc;

    @Override
    public void start(Stage stage) {

        window = stage;

        // ================= CONNECT RMI =================
        try {
            Registry registry = LocateRegistry.getRegistry("localhost", 1099);
            bank = (BankRemote) registry.lookup("BankService");
        } catch (Exception e) {
            System.out.println("Server not found");
        }

        // ================= LOGIN SCREEN =================
        Label accLabel = new Label("Account Number:");
        TextField accField = new TextField();

        Label pinLabel = new Label("PIN:");
        PasswordField pinField = new PasswordField();

        Button loginBtn = new Button("Login");
        Label loginStatus = new Label();

        VBox loginLayout = new VBox(10,
                accLabel, accField,
                pinLabel, pinField,
                loginBtn,
                loginStatus
        );
        loginLayout.setStyle("-fx-padding:20");

        loginScene = new Scene(loginLayout, 300, 250);

        // ================= ADMIN SCREEN =================
        Label adminTitle = new Label("ADMIN DASHBOARD");

        Label nameLabel = new Label("Name:");
        TextField nameField = new TextField();

        Label pinCreateLabel = new Label("PIN:");
        PasswordField newPinField = new PasswordField();

        Button createBtn = new Button("Create Account");
        Button adminLogout = new Button("Logout");

        Label adminStatus = new Label();

        VBox adminLayout = new VBox(10,
                adminTitle,
                nameLabel, nameField,
                pinCreateLabel, newPinField,
                createBtn,
                adminStatus,
                adminLogout
        );

        adminLayout.setStyle("-fx-padding:20");
        adminScene = new Scene(adminLayout, 300, 300);

        // ================= USER SCREEN =================
        Label userTitle = new Label("USER DASHBOARD");

        Label amountLabel = new Label("Amount:");
        TextField amountField = new TextField();

        Label toLabel = new Label("To Account:");
        TextField toField = new TextField();

        Button depBtn = new Button("Deposit");
        Button withBtn = new Button("Withdraw");
        Button transBtn = new Button("Transfer");
        Button balBtn = new Button("Balance");
        Button histBtn = new Button("History");
        Button userLogout = new Button("Logout");

        Label userStatus = new Label();

        VBox userLayout = new VBox(10,
                userTitle,
                amountLabel, amountField,
                toLabel, toField,
                depBtn,
                withBtn,
                transBtn,
                balBtn,
                histBtn,
                userStatus,
                userLogout
        );

        userLayout.setStyle("-fx-padding:20");
        userScene = new Scene(userLayout, 350, 450);

        // ================= LOGIN ACTION =================
        loginBtn.setOnAction(e -> {
            try {
                int acc = Integer.parseInt(accField.getText());
                String pin = pinField.getText();

                boolean ok = bank.login(acc, pin);

                if (!ok) {
                    loginStatus.setText("❌ Login Failed");
                    return;
                }

                currentAcc = acc;
                String role = bank.getRole(acc);

                if (role.equals("ADMIN")) {
                    window.setScene(adminScene);
                } else {
                    window.setScene(userScene);
                }

            } catch (Exception ex) {
                loginStatus.setText("❌ Error");
            }
        });

        // ================= ADMIN ACTION =================
        createBtn.setOnAction(e -> {
            try {
                String name = nameField.getText();
                String pin = newPinField.getText();

                int acc = bank.createAccount(name, pin, true);

                if (acc == -2) {
                    adminStatus.setText("⚠ Account already exists!");
                } else if (acc == -3) {
                    adminStatus.setText("⚠ Max accounts reached!");
                } else if (acc == -1) {
                    adminStatus.setText("❌ Not allowed");
                } else {
                    adminStatus.setText("✅ Account created: " + acc);
                }

            } catch (Exception ex) {
                adminStatus.setText("Error creating account");
            }
        });

        adminLogout.setOnAction(e -> {
            try {
                bank.logout(currentAcc);
                window.setScene(loginScene);
            } catch (Exception ex) {
                System.out.println("Logout error");
            }
        });

        // ================= USER ACTION =================
        depBtn.setOnAction(e -> {
            try {
                double amt = Double.parseDouble(amountField.getText());
                userStatus.setText(bank.deposit(currentAcc, amt));
            } catch (Exception ex) {
                userStatus.setText("Error deposit");
            }
        });

        withBtn.setOnAction(e -> {
            try {
                double amt = Double.parseDouble(amountField.getText());
                userStatus.setText(bank.withdraw(currentAcc, amt));
            } catch (Exception ex) {
                userStatus.setText("Withdraw error");
            }
        });

        transBtn.setOnAction(e -> {
            try {
                int to = Integer.parseInt(toField.getText());
                double amt = Double.parseDouble(amountField.getText());
                userStatus.setText(bank.transfer(currentAcc, to, amt));
            } catch (Exception ex) {
                userStatus.setText("Transfer error");
            }
        });

        balBtn.setOnAction(e -> {
            try {
                double bal = bank.getBalance(currentAcc);
                userStatus.setText("Balance: " + bal);
            } catch (Exception ex) {
                userStatus.setText("Balance error");
            }
        });

        histBtn.setOnAction(e -> {
            try {
                StringBuilder sb = new StringBuilder();
                for (String h : bank.getTransactionHistory(currentAcc)) {
                    sb.append(h).append("\n");
                }
                userStatus.setText(sb.toString());
            } catch (Exception ex) {
                userStatus.setText("History error");
            }
        });

        userLogout.setOnAction(e -> {
            try {
                bank.logout(currentAcc);
                window.setScene(loginScene);
            } catch (Exception ex) {
                System.out.println("Logout error");
            }
        });

        // ================= START =================
        window.setScene(loginScene);
        window.setTitle("Bank System");
        window.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}