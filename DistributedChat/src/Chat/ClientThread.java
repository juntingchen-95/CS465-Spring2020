package Chat;

import Message.*;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class ClientThread implements Runnable {

    private Thread clientThread;
    private Socket socket;
    private ChatNode.NodeInfo successorNode;

    public ClientThread(String ipAddress, int port) throws IOException {
        socket = new Socket(ipAddress, port);
        successorNode = new ChatNode.NodeInfo();
        successorNode.setIpAddress(ipAddress);
        successorNode.setPort(port);
    }

    public void start() {
        if (clientThread == null) {
            clientThread = new Thread(this, "serverThread");
            clientThread.start();
        }
    }

    @Override
    public void run() {
        boolean quit = false;
        try {
            while (!quit) {
                boolean reConnect = false;
                ObjectOutputStream objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
                while (!reConnect) {
                    synchronized (ChatNode.messageQueue) {
                        if (ChatNode.messageQueue.isEmpty()) {
                            try {
                                ChatNode.messageQueue.wait();
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        } else {
                            Message message = ChatNode.messageQueue.poll();
                            switch (message.getType()) {
                                case "initialize":
                                case "join":
                                case "normal":
                                    objectOutputStream.writeObject(message);
                                    objectOutputStream.flush();
                                    break;
                                case "connectNewNode":
                                    socket.shutdownOutput();
                                    socket.close();
                                    ChatNode.NodeInfo newNodeInfo = ((MessageUtility) message).getNodeInfo();
                                    socket = new Socket(newNodeInfo.getIpAddress(), newNodeInfo.getPort());
                                    objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
                                    objectOutputStream.writeObject(new MessageUtility("connectNewNode2", successorNode));
                                    objectOutputStream.flush();
                                    successorNode = newNodeInfo;
                                    break;
                                case "connectNewNode2":
                                    synchronized (ChatNode.nodeInfo) {
                                        if (!ChatNode.nodeInfo.getInitializeStatus()) {
                                            ChatNode.nodeInfo.setIpAddress(((MessageUtility) message).getNodeInfo().getIpAddress());
                                            ChatNode.nodeInfo.setInitializeStatus(true);
                                        }
                                    }
                                    socket.shutdownOutput();
                                    socket.close();
                                    newNodeInfo = ((MessageUtility) message).getNodeInfo();
                                    socket = new Socket(newNodeInfo.getIpAddress(), newNodeInfo.getPort());
                                    objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
                                    successorNode = newNodeInfo;
                                    break;
                                case "selfLeave":
                                    objectOutputStream.writeObject(
                                            new MessageLeave("leave", ((MessageUtility) message).getNodeInfo(), successorNode)
                                    );
                                    objectOutputStream.flush();
                                    reConnect = true;
                                    quit = true;
                                    break;
                                case "leave":
                                    ChatNode.NodeInfo leaveNodeInfo = ((MessageLeave) message).getNodeInfo();
                                    System.out.println("get leave test");
                                    if (leaveNodeInfo.getDescription().equals(successorNode.getDescription())) {
                                        System.out.println("get leave");
                                        objectOutputStream.writeObject(new MessageUtility("quit", successorNode));
                                        objectOutputStream.flush();
                                        socket.shutdownOutput();
                                        socket.close();
                                        newNodeInfo = ((MessageLeave) message).getSuccessorNodeInfo();
                                        socket = new Socket(newNodeInfo.getIpAddress(), newNodeInfo.getPort());
                                        successorNode = newNodeInfo;
                                    } else {
                                        objectOutputStream.writeObject(message);
                                        objectOutputStream.flush();
                                    }
                                    break;
                            }
                        }
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
