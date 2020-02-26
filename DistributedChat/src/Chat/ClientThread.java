package Chat;

import Message.*;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class ClientThread implements Runnable {

    private Thread clientThread;
    private Socket socket;

    public ClientThread(String ipAddress, int port) throws IOException {
        socket = new Socket(ipAddress, port);
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
                                    ChatNode.NodeInfo nodeInfo = ((MessageUtility) message).getNodeInfo();
                                    socket = new Socket(nodeInfo.getIpAddress(), nodeInfo.getPort());
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
