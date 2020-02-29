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
    public static int listeningPort;
    
    public static void main(String[] args) throws IOException {
        messageQueue = new ConcurrentLinkedDeque<Message>();
        nodeInfo = new NodeInfo();
        initialize();
    }

    private static void initialize() throws IOException {
        Scanner in = new Scanner(System.in);

        System.out.println("######################\n#  Distributed Chat  #\n######################");

        boolean portCheck = false;
        while (!portCheck) {
            System.out.println("\nPlease set up a listening port: ");
            listeningPort = Integer.parseInt(in.nextLine());
            if (!Util.checkPort(listeningPort)) {
                System.out.println("Incorrect value of port: must be between 1 and 65,535");
            } else {
                portCheck = true;
            }
        }
        ServerThread serverThread = new ServerThread(listeningPort);
        serverThread.start();
        nodeInfo.setPort(listeningPort);
        
        System.out.println("Your Network information is: ");
        Util.listNetworkInterfaces();
        System.out.println("Type a command to begin (/help for a list of the commands)");

        String userCommand = in.nextLine();

        if (userCommand.equals(Util.helpCommand)) {
            Util.displayHelpCommand();
        } else {
            StringTokenizer tokenizer = new StringTokenizer(userCommand, " ");
            // Switch over the first token, i.e., the command of the user
            switch (tokenizer.nextToken()) {
                case Util.startCommand:
                    setup(userCommand, "initialize");
                    break;
                case Util.joinCommand: // The user wants to join an existing chat
                    setup(userCommand, "join");
                    break;
                default:
                    System.out.println("Unknown command. Type /help to display the available commands");
                    break;
            }
        }

        while (true) {
            System.out.println("Please input chat message");
            String messageString = in.nextLine();
            synchronized (messageQueue) {
                if (messageString.equals("/quit")) {
                    messageQueue.offer(new MessageUtility("selfLeave", nodeInfo));
                } else {
                    messageQueue.offer(new MessageNormal("normal", messageString, nodeInfo));
                }
                messageQueue.notify();
            }
        }
    }

    private static void setup(String command, String messageType) throws IOException {
    	if (messageType.equals("initialize")) {
    		InetAddress addr = InetAddress.getLocalHost();
    		String ipAddress = addr.getHostAddress();
    		clientThread = new ClientThread(ipAddress, listeningPort);
            clientThread.start();
            NodeInfo nodeInfo = new NodeInfo();
            nodeInfo.setIpAddress(ipAddress);
    	}
    	else {
    		StringTokenizer tokenizer = new StringTokenizer(command, " ");

    		// Check there are 3 tokens: the command, the IP address, and the port
    		if (tokenizer.countTokens() != 3) {
    			System.out.println("Incorrect join command: the IP address and the port to join are needed");
    			return;
    		}

    		// Discard the first token, i.e., /join
    		tokenizer.nextToken();

    		// Retrieve the second token, i.e., the IP address of the server to join
    		String ipAddress = tokenizer.nextToken();
    		if (!Util.checkIPv4Address(ipAddress)) {
    			System.out.println("The IP address is not correctly formatted. Correct format: xxx.xxx.xxx.xxx, xxx between 0 and 255");
    			return;
    		}

    		// Retrieve the third token, i.e., the port of the server to join
    		int port = Integer.parseInt(tokenizer.nextToken());
    		if (!Util.checkPort(port)) {
    			System.out.println("Incorrect value of port: must be between 1 and 65,535");
    			return;
    		}

    		InetAddress inetAddress = null;
    		try {
    			inetAddress = InetAddress.getByName(ipAddress);
    		} catch (UnknownHostException e) {
    			e.printStackTrace();
    		}

    		if (inetAddress == null) {
    			System.out.println("Could not find the IP address " + ipAddress);
    			return;
    		}

    		clientThread = new ClientThread(ipAddress, port);
    		clientThread.start();
    		NodeInfo nodeInfo = new NodeInfo();
    		nodeInfo.setPort(listeningPort);
    		
    		synchronized (messageQueue) {
    			messageQueue.offer(new MessageUtility(messageType, nodeInfo));
    			messageQueue.notify();
    		}
    	}
    }

    public static class NodeInfo implements Serializable {

        private String ipAddress;
        private int port;
        private boolean initializeStatus = false;

        public void setIpAddress(String ipAddress) {
            this.ipAddress = ipAddress;
        }

        public void setPort(int port) {
            this.port = port;
        }

        public void setInitializeStatus(boolean status) {
            this.initializeStatus = status;
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

        public boolean getInitializeStatus() {
            return initializeStatus;
        }
    }
}
