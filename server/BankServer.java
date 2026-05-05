package server;

import common.BankRemote;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class BankServer {

    public static void main(String[] args) {

        try {
            // Start registry on port 1099
            Registry registry = LocateRegistry.createRegistry(1099);

            // Create server object
            BankRemote bank = new BankImpl();

            // Bind object to name
            registry.rebind("BankService", bank);

            System.out.println("Bank Server is running...");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}