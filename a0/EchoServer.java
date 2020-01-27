package a0;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class EchoServer {

    public static void main(String []args) throws IOException {
        ServerSocket server = new ServerSocket(1234);
        Socket client;
        while (true) {
            System.out.println("Waiting for connections from client.");
            client = server.accept();
            System.out.println("Client (IP: " + client.getInetAddress().getHostAddress() +") connected.");
            EchoThread echoThread = new EchoThread(client);
            Thread clientThread = new Thread(echoThread);
            clientThread.start();
        }
    }
}