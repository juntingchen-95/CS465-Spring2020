package Chat;

import Util.Util;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class ClientThread implements Runnable
{
    private Thread clientThread;
    private Client nextClient;
    private Socket nextClientSocket;
    private ObjectOutputStream outputStream;


    public ClientThread(Client nextClient, Socket nextClientSocket)
    {
        this.nextClient = nextClient;
        this.nextClientSocket = nextClientSocket;
    }


    public void start()
    {
        if (null == clientThread)
        {
            clientThread = new Thread(this, "clientThread");
            clientThread.start();
        }
    }


    @Override
    public void run()
    {
        try
        {
            outputStream = new ObjectOutputStream(nextClientSocket.getOutputStream());
        } catch (IOException e)
        {
            e.printStackTrace();
        }

        String userInput = "";
        while (!userInput.equals(Util.quitCommand))
        {

        }
    }


    public static void updateNextNode(Client nextClient, Socket nextClientSocket)
    {

    }


    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////


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
}
