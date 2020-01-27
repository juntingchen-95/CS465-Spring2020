package a0;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class EchoServer {

    // The main method of the Echo Server.
    public static void main(String []args) throws IOException {
        // Open a socket port at server part.
        ServerSocket server = new ServerSocket(1234);
        Socket client;
        // The server loop.
        while (true) {
            System.out.println("Waiting for connections from client.");
            // Wait the client connection.
            client = server.accept();
            System.out.println("Client (IP: " + client.getInetAddress().getHostAddress() +") connected.");
            // If a client connected, start a new thread.
            EchoThread echoThread = new EchoThread(client);
            Thread clientThread = new Thread(echoThread);
            clientThread.start();
        }
    }
}
