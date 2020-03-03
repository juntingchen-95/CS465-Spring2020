package Chat;

import Message.*;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.atomic.AtomicBoolean;

public class ServerThread implements Runnable {

    private Thread serverThread;
    private ServerSocket serverSocket;
    public static AtomicBoolean quit = new AtomicBoolean(false);

    public ServerThread(int listeningPort) throws IOException {
        serverSocket = new ServerSocket(listeningPort);
        System.out.println("System message: Node start listen port " + listeningPort);
    }

    public void start() {
        if (serverThread == null) {
            serverThread = new Thread(this, "serverThread");
            serverThread.start();
        }
    }

    @Override
    public void run() {
        Socket client;
        // If there is a new connection, start a new child thread to process
        while (quit.compareAndSet(false, false)) {
            try {
                client = serverSocket.accept();
                System.out.println("System message: New node (IP: " + client.getInetAddress().getHostAddress() +") connected.");
                ServerListeningThread serverListeningThread = new ServerListeningThread(client);
                serverListeningThread.start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static class ServerListeningThread implements Runnable {

        private Thread serverListeningThread;
        private Socket client;

        public ServerListeningThread(Socket client) {
            this.client = client;
        }

        public void start() {
            if (serverListeningThread == null) {
                serverListeningThread = new Thread(this, "serverListeningThread");
                serverListeningThread.start();
            }
        }

        @Override
        public void run() {
            try {
                boolean quitThread = false;
                ObjectInputStream objectInputStream = new ObjectInputStream(client.getInputStream());
                while (!quitThread) {
                    // Read message object form input stream
                    Message message = (Message) objectInputStream.readObject();
                    // Determine the different message types and process them accordingly
                    switch (message.getType()) {
                        case "join":
                            // If receive join request, add connectNewNode message in the message queue
                            synchronized (ChatNode.messageQueue) {
                                ChatNode.messageQueue.offer(new MessageUtility("connectNewNode", ((MessageUtility) message).getNodeInfo()));
                                ChatNode.messageQueue.notify();
                            }
                            break;
                        case "connectNewNode":
                        case "connectNewNode2":
                        case "leave":
                            // If receive connectNewNode, connectNewNode2, and leave message, add it in the message queue directly
                            synchronized (ChatNode.messageQueue) {
                                ChatNode.messageQueue.offer(message);
                                ChatNode.messageQueue.notify();
                            }
                            break;
                        case "normal":
                            // If receive normal message, if the sender is not the node itself, print the message and add it
                            // in the message queue. Otherwise ignore the message
                            ChatNode.NodeInfo senderNode = ((MessageNormal) message).getNodeInfo();
                            synchronized (ChatNode.nodeInfo) {
                                if (!senderNode.getDescription().equals(ChatNode.nodeInfo.getDescription())) {
                                    System.out.println("User message (From " + ((MessageNormal) message).getNodeInfo().getDescription()
                                            + "): " + ((MessageNormal) message).getContent());
                                    synchronized (ChatNode.messageQueue) {
                                        ChatNode.messageQueue.offer(message);
                                        ChatNode.messageQueue.notify();
                                    }
                                }
                            }
                            break;
                        case "quit":
                            // If receive quit request, the server thread will exit
                            synchronized (ServerThread.quit) {
                                ServerThread.quit.compareAndSet(false, true);
                            }
                            quitThread = true;
                            System.exit(0);
                            break;
                    }
                }
            } catch (IOException | ClassNotFoundException e) {
                System.out.println("System message: Node (IP: " + client.getInetAddress().getHostAddress() +") disconnected.");
            } finally {
                try {
                    client.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
