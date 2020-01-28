import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import static java.lang.System.out;

// Names
// https://youtu.be/Bm2NhvO4dMY

public class EchoServer_2
{
    public static final int port = 2222;

    public static void main(String[] args)
    {
        try
        {
            // Create a server socket attached to the port defined above
            ServerSocket serverSocket = new ServerSocket(port);
            out.println("Server started on port " + port);

            // Infinite loop making the server to run indefinitely
            while (true)
            {
                // Creating a new socket for each new client
                Socket socket = serverSocket.accept();
                out.println("New client connected");
                // Spawning a new thread for each new client so it does not block the server
                EchoThread_2 thread = new EchoThread_2(socket);
                thread.start();
            }
        } catch (IOException e)
        {
            e.printStackTrace();
        }
    }

}
