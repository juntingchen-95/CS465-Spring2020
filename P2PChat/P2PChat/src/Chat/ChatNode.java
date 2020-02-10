package Chat;

import java.net.ServerSocket;

public class ChatNode
{
    private ServerThread serverThread;
    private ClientThread clientThread;
    private Client localClient;
    private ServerSocket incomingClient;

    public static void main(String[] args)
    {

    }

    public void waitForNewClient()
    {

    }

    public ServerThread getServerThread()
    {
        return serverThread;
    }

    public ClientThread getClientThread()
    {
        return clientThread;
    }

    public Client getLocalClient()
    {
        return localClient;
    }
}
