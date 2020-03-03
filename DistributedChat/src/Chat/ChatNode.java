package Chat;

import Message.*;
import Util.Util;

import java.io.IOException;
import java.io.Serializable;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Queue;
import java.util.Scanner;
import java.util.StringTokenizer;
import java.util.concurrent.ConcurrentLinkedDeque;

public class ChatNode {

    public static Queue<Message> messageQueue;
    public static ClientThread clientThread;
    public static NodeInfo nodeInfo;
    
    public static void main(String[] args) throws IOException {
        messageQueue = new ConcurrentLinkedDeque<Message>();
        nodeInfo = new NodeInfo();
        run();
    }

    /**
     * Function to initialize the chat node and read user's inputs
     */
    private static void run() throws IOException {
        Scanner in = new Scanner(System.in);

        System.out.println("######################\n#  Distributed Chat  #\n######################");
        Util.listNetworkInterfaces();

        // Setup listening port and start listening thread
        while (true) {
            System.out.println("\nPlease set up IP address and listening port: ");
            try {
                StringTokenizer tokenizer = new StringTokenizer(in.nextLine(), " ");

                // Check there are 2 tokens: the IP address and the port
                if (tokenizer.countTokens() != 2) {
                    System.out.println("Incorrect input: the IP address and the port are needed");
                    continue;
                }

                // Retrieve IP address
                String ipAddress = tokenizer.nextToken();
                if (!Util.checkIPv4Address(ipAddress)) {
                    System.out.println("The IP address is not correctly formatted. Correct format: xxx.xxx.xxx.xxx, xxx between 0 and 255");
                    continue;
                }
                nodeInfo.setIpAddress(ipAddress);

                // Retrieve listening port
                int port = Integer.parseInt(tokenizer.nextToken());
                if (!Util.checkPort(port)) {
                    System.out.println("Incorrect value of port: must be between 1 and 65,535");
                    continue;
                }
                nodeInfo.setPort(port);
                break;
            } catch (NumberFormatException e) {
                System.out.println("Incorrect value of port: must be between 1 and 65,535");
            }
        }
        ServerThread serverThread = new ServerThread(nodeInfo.getPort());
        serverThread.start();

        // Command selector
        System.out.println("\nType a command to begin (/help for a list of the commands)");
        boolean isContinue = false;
        while (!isContinue) {
            String userCommand = in.nextLine();
            if (userCommand.equals(Util.helpCommand)) {
                Util.displayHelpCommand();
            } else {
                StringTokenizer tokenizer = new StringTokenizer(userCommand, " ");
                // Switch over the first token, i.e., the command of the user
                switch (tokenizer.nextToken()) {
                    case Util.startCommand: // The user wants to start a new chat
                        isContinue = setup(userCommand, "initialize");
                        break;
                    case Util.joinCommand: // The user wants to join an existing chat
                        isContinue = setup(userCommand, "join");
                        break;
                    case Util.listCommand: // The user wants to list the available network interfaces on the machine
                        Util.listNetworkInterfaces();
                        break;
                    case Util.quitCommand: // The user wants to quit the program
                        System.out.println("Bye");
                        System.exit(0);
                        break;
                    default:
                        System.out.println("Unknown command. Type /help to display the available commands");
                        break;
                }
            }
        }

        // Chat message input loop
        boolean quit = false;
        while (!quit) {
            String messageString = in.nextLine();
            synchronized (messageQueue) {
                // If input quit command, it will add selfLeave message in the message queue, other wise add normal message
                if (messageString.equals(Util.quitCommand)) {
                    messageQueue.offer(new MessageUtility("selfLeave", nodeInfo));
                    quit = true;
                } else {
                    messageQueue.offer(new MessageNormal("normal", messageString, nodeInfo));
                }
                messageQueue.notify();
            }
        }
        System.out.println("Bye");
    }

    /**
     * Function tu setup the node (set to the initial node or join other node)
     * @param command Correctly formatted /start or /join command with the IP address and the port to connect to.
     * @param messageType initialize or join
     */
    private static boolean setup(String command, String messageType) throws IOException {
        if (messageType.equals("initialize")) {
            // Start sending thread
            clientThread = new ClientThread(nodeInfo.getIpAddress(), nodeInfo.getPort());
            clientThread.start();
            System.out.println("The initial node setting is: " + nodeInfo.getIpAddress() + ":" + nodeInfo.getPort());
        } else {
            StringTokenizer tokenizer = new StringTokenizer(command, " ");

            // Check there are 3 tokens: the command, the IP address, and the port
            if (tokenizer.countTokens() != 3) {
                System.out.println("Incorrect join command: the IP address and the port to join are needed");
                return false;
            }

            // Discard the first token, i.e., /join
            tokenizer.nextToken();

            // Retrieve the second token, i.e., the IP address of the server to join
            String ipAddress = tokenizer.nextToken();
            if (!Util.checkIPv4Address(ipAddress)) {
                System.out.println("The IP address is not correctly formatted. Correct format: xxx.xxx.xxx.xxx, xxx between 0 and 255");
                return false;
            }

            // Retrieve the third token, i.e., the port of the server to join
            int port = Integer.parseInt(tokenizer.nextToken());
            if (!Util.checkPort(port)) {
                System.out.println("Incorrect value of port: must be between 1 and 65,535");
                return false;
            }

            // Check IP address
            InetAddress inetAddress = null;
            try {
                inetAddress = InetAddress.getByName(ipAddress);
            } catch (UnknownHostException e) {
                e.printStackTrace();
            }

            if (inetAddress == null) {
                System.out.println("Could not find the IP address " + ipAddress);
                return false;
            }

            // Start sending thread and send join request to other node
            clientThread = new ClientThread(ipAddress, port);
            clientThread.start();
            synchronized (messageQueue) {
                messageQueue.offer(new MessageUtility(messageType, nodeInfo));
                messageQueue.notify();
            }
        }
        return true;
    }

    /**
     * The class to record node information (IP address and listening port)
     */
    public static class NodeInfo implements Serializable {

        private String ipAddress;
        private int port;

        public void setIpAddress(String ipAddress) {
            this.ipAddress = ipAddress;
        }

        public void setPort(int port) {
            this.port = port;
        }

        public String getIpAddress() {
            return ipAddress;
        }

        public int getPort() {
            return port;
        }

        public String getDescription() {
            return ipAddress + ":" + port;
        }
    }
}
