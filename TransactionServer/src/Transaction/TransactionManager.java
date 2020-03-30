package Transaction;

import Message.*;
import Account.AccountManager;
import Server.TransactionServer;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;

public class TransactionManager implements MessageTypes {

    private int transactionCounter;
    private ArrayList<Transaction> transactionList;

    public TransactionManager() {
        transactionCounter = 0;
        transactionList = new ArrayList<>();
    }

    public ArrayList<Transaction> getTransactionList() {
        return transactionList;
    }

    public void run(Socket client) {
        (new Thread(new TransactionManagerWorker(client))).start();
    }

    public class TransactionManagerWorker implements Runnable {

        Socket client;

        public TransactionManagerWorker(Socket client) {
            this.client = client;
        }

        @Override
        public void run() {
            try {
                ObjectInputStream objectInputStream = new ObjectInputStream(client.getInputStream());
                ObjectOutputStream objectOutputStream = new ObjectOutputStream(client.getOutputStream());
                boolean keepGoing = true;
                Transaction transaction = null;
                Message message;
                while (keepGoing) {
                    message = (Message) objectInputStream.readObject();
                    switch (message.getMessageType()) {
                        case OPEN_TRANSACTION:
                            synchronized (transactionList) {
                                transaction = new Transaction(transactionCounter++);
                                transactionList.add(transaction);
                            }
                            objectOutputStream.writeObject(transaction.getTransactionID());
                            break;
                        case CLOSE_TRANSACTION:
                            transactionList.remove(transaction);
                            objectInputStream.close();
                            objectOutputStream.close();
                            keepGoing = false;
                            break;
                        case READ_REQUEST:
                            int accountNumber = message.getAccountNumber();
                            int balance = TransactionServer.accountManager.read(accountNumber);
                            objectOutputStream.writeObject(balance);
                            break;
                        case WRITE_REQUEST:
                            break;
                    }
                }
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            } finally {
                try {
                    client.shutdownInput();
                    client.shutdownOutput();
                    client.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
