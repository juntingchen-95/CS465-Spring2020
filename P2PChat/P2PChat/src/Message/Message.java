package Message;

import Chat.Client;

import java.io.Serializable;

public class Message implements Serializable
{
    private final Client originMessage;
    private final Object message;


    public Message(Client originMessage, Object message)
    {
        this.originMessage = originMessage;
        this.message = message;
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////


    public Client getOriginMessage()
    {
        return originMessage;
    }


    public Object getMessage()
    {
        return message;
    }

}
