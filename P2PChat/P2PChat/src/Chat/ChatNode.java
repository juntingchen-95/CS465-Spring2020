package Chat;

import Message.Message;
import Util.Util;

import java.io.IOException;
import java.net.*;
import java.util.Queue;
import java.util.Scanner;
import java.util.StringTokenizer;
import java.util.concurrent.ConcurrentLinkedQueue;

public class ChatNode
{
    private static ServerThread serverThread;   // Thread handling the server side of the chat (incoming messages from previous node)
    private static ClientThread clientThread;   // Thread handling user's interactions and sending messages to the next node
    private static Client localClient;          // Information about the client of the current node (i.e., the current user)
    private static ServerSocket incomingClient; // ServerSocket staying open for future connections

    public static Object interruptClient;      // Object to notify the client that it should interrupt its execution (used as a mutex)
    public static Object interruptServer;      // Object to notify the server that it should interrupt its execution (used as a mutex)
    public static Boolean clientQuit;          // Object to notify that the user is leaving the chat

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
        interruptClient = new Object();
        interruptServer = new Object();
        clientQuit = false;
        messageQueue = new ConcurrentLinkedQueue<>();
    }


    public static void main(String[] args)
    {
        // Initialize the chat: either connect to an existing chat, start a new chat, or leave the application
        initializeChat();
        // Listen to new clients willing to connect to this node on the incomingClient ServerSocket
        waitForNewClient();
    }


    /**
     * Function to read user's command(s) when starting the application, e.g., joining an existing chat, creating
     * a new chat, etc.
     */
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
                StringTokenizer tokenizer = new StringTokenizer(userCommand, " ");
                // Switch over the first token, i.e., the command of the user
                switch (tokenizer.nextToken())
                {
                    case Util.quitCommand: // The user is leaving the application
                        System.exit(0);
                    case Util.listCommand: // The user wants to list the available network interfaces on the machine
                        Util.listNetworkInterfaces();
                        break;
                    case Util.joinCommand: // The user wants to join an existing chat
                        stopReadInput = joinCommand(userCommand);
                        break;
                    case Util.startCommand: // The user wants to create a new chat
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
     * @param command Correctly formatted /join command with the IP address and the port to connect to.
     * @return True if a connection has been established, false otherwise.
     */
    public static boolean joinCommand(String command)
    {
        StringTokenizer tokenizer = new StringTokenizer(command, " ");

        // Check there are 3 tokens: the command, the IP address, and the port
        if (3 != tokenizer.countTokens())
        {
            System.out.println("Incorrect join command: the IP address and the port to join are needed");
            return false;
        }

        // Discard the first token, i.e., /join
        tokenizer.nextToken();

        // Retrieve the second token, i.e., the IP address of the server to join
        String ipAddress = tokenizer.nextToken();
        if (!Util.checkIPv4Address(ipAddress))
        {
            System.out.println("The IP address is not correctly formatted. Correct format: xxx.xxx.xxx.xxx, xxx between 0 and 255");
            return false;
        }

        // Retrieve the third token, i.e., the port of the server to join
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
                System.out.println("Connected to " + ipAddress + "/" + port);
                // Start the server thread, without the information of the precedent node yet (will come after the join message
                // from this client crosses the ring, and with the precedent node initializing a connection to this client)
                serverThread = new ServerThread(localClient, null);
                serverThread.start();
                // Start the client thread, connected to the client the user chose to connect to
                clientThread = new ClientThread(localClient, socket);
                clientThread.start();
                return true;
            }
        }
    }

    /**
     * Function to handle the /start command at the beginning of the application. This function checks that the command
     * is correctly used (with a correct IP address and port), and tries to start a new chat on the user's machine.
     * @param command Correctly formatted /start command with the IP address and the port to start the chat on.
     * @return True if a new chat has been created, false otherwise.
     */
    public static boolean startCommand(String command)
    {
        StringTokenizer tokenizer = new StringTokenizer(command, " ");

        // Check that there are 3 tokens: the command, the IP address, and the port
        if (3 != tokenizer.countTokens())
        {
            System.out.println("Incorrect start command: the IP address and the port to join are needed");
            return false;
        }

        // Discard the first token, i.e., the command
        tokenizer.nextToken();

        // Retrieve the second token, i.e., the IP address to start the server on
        String ipAddress = tokenizer.nextToken();
        if (!Util.checkIPv4Address(ipAddress))
        {
            System.out.println("The IP address is not correctly formatted. Correct format: xxx.xxx.xxx.xxx, xxx between 0 and 255");
            return false;
        }

        // Retrieve the third token, i.e., the port to start the server on
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
                // Create a new ServerSocket on the given IP address and port, accepting 1 incoming connection
                // Note: 1 is arbitrary, and can be changed if needed (although probably not)
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
                System.out.println("Chat started on " + ipAddress + "/" + port);
                // Start the server thread with no attached socket (as the chat just started and only the current user is connected)
                serverThread = new ServerThread(localClient, null);
                serverThread.start();
                // Start the client thread with no attached socket (as the chat just started and only the current user is connected)
                clientThread = new ClientThread(localClient, null);
                clientThread.start();
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
                // Safely update the server side of the application
                synchronized (interruptServer)
                {
                    serverThread.updatePrecNode(socketIncomingClient);
                    interruptServer.notify();
                }
            }

            // Safely check that the client is not leaving the chat
            synchronized (clientQuit)
            {
                localClientQuit = clientQuit;
            }
        }

        // The client is leaving the chat, so we close our ServerSocket
        try
        {
            socketIncomingClient.close();
        } catch (IOException e)
        {
            e.printStackTrace();
        }
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////


    public static ServerThread getServerThread()
    {
        return serverThread;
    }

    public
    static ClientThread getClientThread()
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
