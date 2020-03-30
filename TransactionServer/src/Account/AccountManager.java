package Account;

public class AccountManager {

    private Account[] accountArray;

    public AccountManager(int accountNumber, int initialBalance) {
        accountArray = new Account[accountNumber];
        for (int i = 0; i < accountNumber; i++) {
            accountArray[i] = new Account(i, initialBalance);
        }
    }

    public int read(int accountID) {
        return accountArray[accountID].getBalance();
    }

    public void write(int accountID, int newBalance) {
        accountArray[accountID].setBalance(newBalance);
    }
}