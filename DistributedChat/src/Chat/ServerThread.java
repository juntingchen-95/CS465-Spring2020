package Chat;

import Message.*;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class ServerThread implements Runnable {

    private Thread serverThread;
    private ServerSocket serverSocket;

    public ServerThread(int listeningPort) throws IOException {
        serverSocket = new ServerSocket(listeningPort);
        System.out.println("Node start listen port " + listeningPort);
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
        boolean quit = false;
        while (!quit) {
            try {
                client = serverSocket.accept();
                System.out.println("New node (IP: " + client.getInetAddress().getHostAddress() +") connected.");
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
                ObjectInputStream objectInputStream = new ObjectInputStream(client.getInputStream());
                while (true) {
                    Message message = (Message) objectInputStream.readObject();
                    switch (message.getType()) {
                        case "initialize":
                            synchronized (ChatNode.nodeInfo) {
                                if (!ChatNode.nodeInfo.getInitializeStatus()) {
                                    ChatNode.nodeInfo.setIpAddress(((MessageUtility) message).getNodeInfo().getIpAddress());
                                    ChatNode.nodeInfo.setInitializeStatus(true);
                                }
                            }
                            break;
                        case "join":
                            ChatNode.NodeInfo nodeInfo = ((MessageUtility) message).getNodeInfo();
                            nodeInfo.setIpAddress(client.getInetAddress().getHostAddress());
                            synchronized (ChatNode.messageQueue) {
                                ChatNode.messageQueue.offer(new MessageUtility("connectNewNode", nodeInfo));
                                ChatNode.messageQueue.notify();
                            }
                            break;
                        case "normal":
                            ChatNode.NodeInfo senderNode = ((MessageNormal) message).getNodeInfo();
                            synchronized (ChatNode.nodeInfo) {
                                if (!senderNode.getDescription().equals(ChatNode.nodeInfo.getDescription())) {
                                    System.out.println(((MessageNormal) message).getContent());
                                    synchronized (ChatNode.messageQueue) {
                                        ChatNode.messageQueue.offer(message);
                                        ChatNode.messageQueue.notify();
                                    }
                                }
                            }
                            break;
                    }
                }
            } catch (IOException | ClassNotFoundException e) {
                System.out.println("Node (IP: " + client.getInetAddress().getHostAddress() +") disconnected.");
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
