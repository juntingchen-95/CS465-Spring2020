package Message;

import Chat.Client;

public class MessageJoin extends Message
{
    private final Client newClient;
    private final Client connectingTo;

    public MessageJoin(Client originMessage, Object message, Client newClient, Client connectingTo)
    {
        super(originMessage, message);
        this.newClient = newClient;
        this.connectingTo = connectingTo;
    }

    public Client getNewClient()
    {
        return newClient;
    }

    public Client getConnectingTo()
    {
        return connectingTo;
    }
}
