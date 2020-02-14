package Chat;

import Message.Message;

import java.io.ObjectInputStream;
import java.net.Socket;

public class ServerThread implements Runnable
{
    private Thread serverThread;
    private Client precClient;
    private Socket precNodeSocket;
    private ObjectInputStream inputStream;
    private static boolean interrupt;

    public ServerThread(Client precClient, Socket precNodeSocket)
    {
        this.precClient = precClient;
        this.precNodeSocket = precNodeSocket;
    }

    public void start()
    {

    }

    @Override
    public void run()
    {

    }

    public void parseMessage(Message m)
    {

    }

    public void handleJoinMessage(Message m)
    {

    }

    public void handleQuitMessage(Message m)
    {

    }

    public static void updatePrecNode(Client precClient, Socket precClientSocket)
    {

    }

    public Thread getServerThread()
    {
        return serverThread;
    }

    public Client getPrecClient()
    {
        return precClient;
    }

    public Socket getPrecNodeSocket()
    {
        return precNodeSocket;
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
