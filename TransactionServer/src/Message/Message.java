package Message;

import java.io.Serializable;

public class Message implements Serializable {

    private final int messageType;
    private final int accountNumber;

    public Message(int messageType, int accountNumber) {
        this.messageType = messageType;
        this.accountNumber = accountNumber;
    }

    public int getMessageType() {
        return messageType;
    }

    public int getAccountNumber() {
        return accountNumber;
    }
}
