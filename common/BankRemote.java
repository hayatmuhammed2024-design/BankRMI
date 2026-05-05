package common;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface BankRemote extends Remote {

    boolean login(int accountNumber, String pin) throws RemoteException;
    boolean logout(int accountNumber) throws RemoteException;

    String getRole(int accountNumber) throws RemoteException;

    boolean adminLogin(String username, String password) throws RemoteException;

    int createAccount(String name, String pin, boolean isAdmin) throws RemoteException;

    double getBalance(int accountNumber) throws RemoteException;

    String deposit(int accountNumber, double amount) throws RemoteException;

    String withdraw(int accountNumber, double amount) throws RemoteException;

    String transfer(int fromAcc, int toAcc, double amount) throws RemoteException;

    List<String> getTransactionHistory(int accountNumber) throws RemoteException;
}