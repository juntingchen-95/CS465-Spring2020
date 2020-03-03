package Message;

import java.io.Serializable;

public class Message implements Serializable {

    // type record the message type
    private final String type;

    public Message(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }
}
