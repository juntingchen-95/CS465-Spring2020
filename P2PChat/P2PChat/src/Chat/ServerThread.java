package Chat;

import Message.Message;
import Message.MessageJoin;
import Message.MessageQuit;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.Socket;

public class ServerThread implements Runnable
{
    private Thread serverThread;
    private Client precClient;
    private Socket precNodeSocket;
    private ObjectInputStream inputStream;
    private boolean clientQuit;


    public ServerThread(Client precClient, Socket precNodeSocket)
    {
        this.precClient = precClient;
        this.precNodeSocket = precNodeSocket;
        this.serverThread = null;
        this.inputStream = null;
        this.clientQuit = false;
    }


    public void start()
    {
        if (null == serverThread)
        {
            serverThread = new Thread(this, "serverThread");
            serverThread.start();
        }
    }


    @Override
    public void run()
    {
        // Wait for ChatNode to update the socket: either because the user started a new chat, or because the user connected to
        // an existing chat and ChatNode needs to wait for the precedent client to connect, then to update the socket
        synchronized (ChatNode.interruptServer)
        {
            try
            {
                ChatNode.interruptServer.wait();
            } catch (InterruptedException e)
            {
                e.printStackTrace();
            }
        }

        // Retrieve the ObjectInputStream linked to the socket of the precedent node
        try
        {
            inputStream = new ObjectInputStream(precNodeSocket.getInputStream());
        } catch (IOException e)
        {
            e.printStackTrace();
        }

        // While the user does not type the message to quit the chat
        while(!clientQuit)
        {
            // Safely check if the user wants to quit the chat
            synchronized (ChatNode.clientQuit)
            {
                clientQuit = ChatNode.clientQuit;
            }

            Message m = null;
            try
            {
                m = (Message) inputStream.readObject();
            } catch (IOException | ClassNotFoundException e)
            {
                e.printStackTrace();
            }

            // If we read a message from the precedent node
            if (null != m)
            {
                if (m instanceof MessageJoin)
                {
                    handleJoinMessage((MessageJoin) m);
                } else {
                    if (m instanceof MessageQuit)
                    {
                        handleQuitMessage((MessageQuit) m);
                    } else { // m is a regular message
                        parseMessage(m);
                    }
                }
            }
        }
    }


    /**
     * Function to parse a regular (usually text) message from another client of the chat.
     * As it is a regular message, its content is just displayed to the user.
     *
     * @param m The message to parse, containing an object (the load of the message, usually a String).
     */
    public void parseMessage(Message m)
    {
        System.out.println(m.getOriginMessage().getAlias().toUpperCase() + ": " + m.getMessage().toString());
    }


    /**
     * Function to handle the reception of a join message from another client of the chat.
     * This function is not used in the case of a new client connecting to this particular client,
     * and is instead used to be notified of a new client connecting anywhere else in the chat.
     *
     * @param m The message containing the information of the new client and the node it connected to.
     */
    public void handleJoinMessage(MessageJoin m)
    {
        // If the new client connected to the current node's next client (i.e., the new client is inserted between this
        // node and its successor in the ring)
        if (ChatNode.getClientThread().getNextClient().equals(m.getOriginMessage()))
        {
            // Notify the client thread so it can update its successor to the new client
            // Idea: use the queue instead of some booleans trying to synchronize the threads
            ChatNode.getMessageQueue().add(m);
        } else {
            // The new client has connected to this client (us)
            // Note: the socket has already been set in the ChatNode.waitForNewClient function, and should have been
            //       updated in this part of the application as well.
            //       We thus need to update the input stream to read from this new client.
            if (m.getConnectingTo().equals(ChatNode.getLocalClient()))
            {
                try
                {
                    inputStream = new ObjectInputStream(precNodeSocket.getInputStream());
                } catch (IOException e)
                {
                    e.printStackTrace();
                }
            }
            // Else: a client joined the chat, but we are not directly concerned by it. We thus do nothing in particular.
        }
    }


    /**
     * Function to handle the reception of a quit message from another client of the chat.
     * This function is not used if the current user quits the chat, and is instead used to be
     * notified when another client of the chat leaves.
     *
     * @param m The message containing the information about the client that left, as well as who it was connected to.
     */
    public void handleQuitMessage(MessageQuit m)
    {
        // The client leaving the chat is the one directly connected to this node
        // The client connected to this leaving client will thus connected to this client (us)
        if (m.getQuittingClient().equals(this.precClient))
        {
            // Wait on the object to be freed by ChatNode
            synchronized (ChatNode.interruptServer)
            {
                try
                {
                    ChatNode.interruptServer.wait();
                } catch (InterruptedException e)
                {
                    e.printStackTrace();
                }
            }
        } else {
            // The client leaving the chat is the client which we are connected to (i.e., our successor in the chat)
            if (m.getQuittingClient().equals(ChatNode.getClientThread().getNextClient()))
            {
                // Notify the client side of the application that its successor is leaving the chat
                // Idea: use the queue instead of some booleans trying to synchronize the threads
                ChatNode.getMessageQueue().add(m);
            }
            // Else: a client we are not directly connected to left the chat, and so we do not need to do anything in particular.
        }
    }

    /**
     * Update the precedent node after a new client's connection
     * @param precClientSocket The socket of the newly connected client
     */
    public void updatePrecNode(Socket precClientSocket)
    {
        precNodeSocket = precClientSocket;
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////


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
}
