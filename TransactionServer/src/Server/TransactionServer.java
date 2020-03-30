package Server;

import Account.AccountManager;
import Transaction.TransactionManager;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class TransactionServer {

    public static AccountManager accountManager;
    public static TransactionManager transactionManager;

    public static void main(String[] args) throws IOException {
        accountManager = new AccountManager(5, 50);
        transactionManager = new TransactionManager();

        ServerSocket serverSocket = new ServerSocket(1000);
        Socket client;
        while (true) {
            System.out.println("Waiting for connections from client.");
            client = serverSocket.accept();
            System.out.println("Client (IP: " + client.getInetAddress().getHostAddress() +") connected.");
            transactionManager.run(client);
        }
    }
}
