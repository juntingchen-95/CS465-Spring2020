package Chat;

import java.io.ObjectInputStream;
import java.net.Socket;

public class ClientThread implements Runnable
{
    private Thread clientThread;
    private Client nextClient;
    private Socket nextClientSocket;
    private ObjectInputStream inputStream;
    private static boolean interrupt;

    public ClientThread(Client nextClient, Socket nextClientSocket)
    {
        this.nextClient = nextClient;
        this.nextClientSocket = nextClientSocket;
    }

    public void start()
    {

    }

    @Override
    public void run()
    {

    }

    public static void updateNextNode(Client nextClient, Socket nextClientSocket)
    {

    }

    public Thread getClientThread()
    {
        return clientThread;
    }

    public Client getNextClient()
    {
        return nextClient;
    }

    public Socket getNextClientSocket()
    {
        return nextClientSocket;
    }

    public boolean isInterrupt()
    {
        return interrupt;
    }

    public static void setInterrupt(boolean _interrupt)
    {
        interrupt = _interrupt;
    }
}
