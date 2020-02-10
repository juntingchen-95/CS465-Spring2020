package Message;

import Chat.Client;

public class MessageQuit extends Message
{

    private final Client quittingClient;
    private final Client wasConnectedTo;

    public MessageQuit(Client originMessage, Object message, Client quittingClient, Client wasConnectedTo)
    {
        super(originMessage, message);
        this.quittingClient = quittingClient;
        this.wasConnectedTo = wasConnectedTo;
    }

    public Client getQuittingClient()
    {
        return quittingClient;
    }

    public Client getWasConnectedTo()
    {
        return wasConnectedTo;
    }
}
