package Chat;

import Message.Message;
import Util.Util;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Queue;
import java.util.Scanner;
import java.util.StringTokenizer;
import java.util.concurrent.ConcurrentLinkedQueue;

public class ChatNode
{
    private static ServerThread serverThread;   // Thread handling the server side of the chat (incomming messages from previous node)
    private static ClientThread clientThread;   // Thread handling user's interactions and sending messages to the next node
    private static Client localClient;          // Information about the client of the current node (i.e., the current user)
    private static ServerSocket incomingClient; // ServerSocket staying open for future connections

    public static Boolean interruptClient;      // Object to notify the client that it should interrupt its execution (used as a mutex)
    public static Boolean interruptServer;      // Object to notify the server that it should interrupt its execution (used as a mutex)
    public static Boolean clientQuit;           // Object to notify that the user is leaving the chat

    // Idea: use a shared queue to transfer message between the server and the client threads
    // Basically, the server would put the messages it receives and that may concern the client side (typically, if the
    // next node leaves the chat)
    // Eventually create a new type of message, more suited to this type of usage (eventually with a distinction between
    // the client notifying the server or vice versa)
    public static Queue<Message> messageQueue; // Initialized as a ConcurrentLinkedQueue

    public ChatNode()
    {
        serverThread = null;
        clientThread = null;
        localClient = null;
        incomingClient = null;
        interruptClient = false;
        interruptServer = false;
        clientQuit = false;
        messageQueue = new ConcurrentLinkedQueue<>();
    }

    public static void main(String[] args)
    {
        initializeChat();
    }


    public static void initializeChat()
    {
        Scanner in = new Scanner(System.in);

        boolean stopReadInput = false;

        System.out.println("####################");
        System.out.println("#     P2P Chat     #");
        System.out.println("####################");

        System.out.println("Type a command to begin (/help for a list of the commands)");

        while (!stopReadInput)
        {
            String userCommand = in.nextLine();

            if (userCommand.equals(Util.helpCommand))
            {
                Util.displayHelpCommand();
            } else {
                switch (userCommand)
                {
                    case Util.quitCommand:
                        stopReadInput = true;
                        System.exit(0);
                    case Util.joinCommand:
                        stopReadInput = joinCommand(userCommand);
                        break;
                    case Util.startCommand:
                        stopReadInput = startCommand(userCommand);
                        break;
                    default:
                        System.out.println("Unknown command. Type /help to display the available commands");
                        break;
                }
            }
        }
    }

    /**
     * Function to manage the /join command when starting the application. This function checks that the /join command
     * is correctly used (has a correct IP address and port), and then tries to connect to an existing chat.
     * @param command Correctly formated /join command with the IP address and the port to connect to.
     * @return True if a connection has been established, false otherwise.
     */
    public static boolean joinCommand(String command)
    {
        StringTokenizer tokenizer = new StringTokenizer(command, " ");
        if (3 != tokenizer.countTokens())
        {
            System.out.println("Incorrect join command: the IP address and the port to join are needed");
            return false;
        }

        String ipAddress = tokenizer.nextToken();
        if (!Util.checkIPv4Address(ipAddress))
        {
            System.out.println("The IP address is not correctly formated. Correct format: xxx.xxx.xxx.xxx, xxx between 0 and 255");
            return false;
        }
        int port = Integer.parseInt(tokenizer.nextToken());
        if (!Util.checkPort(port))
        {
            System.out.println("Incorrect value of port: must be between 0 and 65,536");
            return false;
        }

        InetAddress inetAddress = null;
        try
        {
            inetAddress = InetAddress.getByName(ipAddress);
        } catch (UnknownHostException e)
        {
            e.printStackTrace();
        }

        if (null == inetAddress)
        {
            System.out.println("Could not find the IP address " + ipAddress);
            return false;
        } else {
            Socket socket = null;
            try
            {
                socket = new Socket(inetAddress, port);
            } catch (IOException e)
            {
                e.printStackTrace();
            }

            if (null == socket)
            {
                System.out.println("Could not connect to the client " + ipAddress + "/" + port);
                return false;
            } else {
                //TODO start a new client thread with the connected socket
                return true;
            }
        }
    }

    /**
     * Function to handle the /start command at the beginning of the application. This function checks that the command
     * is correctly used (with a correct IP address and port), and tries to start a new chat on the user's machine.
     * @param command Correclty formated /start command with the IP address and the port to start the chat on.
     * @return True if a new chat has been created, false otherwise.
     */
    public static boolean startCommand(String command)
    {
        StringTokenizer tokenizer = new StringTokenizer(command, " ");
        if (3 != tokenizer.countTokens())
        {
            System.out.println("Incorrect start command: the IP address and the port to join are needed");
            return false;
        }

        String ipAddress = tokenizer.nextToken();
        if (!Util.checkIPv4Address(ipAddress))
        {
            System.out.println("The IP address is not correctly formated. Correct format: xxx.xxx.xxx.xxx, xxx between 0 and 255");
            return false;
        }
        int port = Integer.parseInt(tokenizer.nextToken());
        if (!Util.checkPort(port))
        {
            System.out.println("Incorrect value of port: must be between 0 and 65,536");
            return false;
        }

        InetAddress inetAddress = null;
        try
        {
            inetAddress = InetAddress.getByName(ipAddress);
        } catch (UnknownHostException e)
        {
            e.printStackTrace();
        }

        if (null == inetAddress)
        {
            System.out.println("Could not find the IP address " + ipAddress);
            return false;
        } else {
            try
            {
                incomingClient = new ServerSocket(port, 1, inetAddress);
            } catch (IOException e)
            {
                e.printStackTrace();
            }

            if (null == incomingClient)
            {
                System.out.println("Could not start a chat on " + ipAddress + "/" + port);
                return false;
            } else {
                //TODO start the rest of the application considering that we are the only user in it
                return true;
            }
        }
    }

    /**
     * Function to accept new clients in the chat. This function loops while the user does not leave the chat.
     */
    public static void waitForNewClient()
    {
        boolean localClientQuit = false;
        Socket socketIncomingClient = null;
        while (!localClientQuit)
        {
            // Safely check that the client is not leaving the chat
            synchronized (clientQuit)
            {
                localClientQuit = clientQuit;
            }

            // Accept incoming connections from new clients
            try
            {
                socketIncomingClient = incomingClient.accept();
            } catch (IOException e)
            {
                e.printStackTrace();
            }

            // A client has connected to this node
            if (null != socketIncomingClient)
            {
                serverThread.updatePrecNode(socketIncomingClient);
            }
        }
    }


    public static ServerThread getServerThread()
    {
        return serverThread;
    }


    public static ClientThread getClientThread()
    {
        return clientThread;
    }


    public static Client getLocalClient()
    {
        return localClient;
    }

    public static Queue<Message> getMessageQueue()
    {
        return messageQueue;
    }
}
