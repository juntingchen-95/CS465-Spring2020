package Transaction;

import Lock.Lock;

import java.util.ArrayList;

public class Transaction {

    private final int transactionID;
    private ArrayList<Lock> lockList;

    public Transaction(int transactionID) {
        this.transactionID = transactionID;
        this.lockList = new ArrayList<>();
    }

    public int getTransactionID() {
        return transactionID;
    }

    public ArrayList<Lock> getLockList() {
        return lockList;
    }

    public void addLock(Lock lock) {
        lockList.add(lock);
    }
}
