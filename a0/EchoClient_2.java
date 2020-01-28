import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ConnectException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class EchoClient_2
{
    public static void main(String[] args)
    {
        Scanner in = new Scanner(System.in);
        System.out.print("Port to connect to: ");
        int port = in.nextInt();

        try
        {
            Socket socket = new Socket("localhost", port);
            if(!socket.isConnected())
            {
                System.out.println("Impossible to connect to the server");
            }

            DataInputStream dis = new DataInputStream(socket.getInputStream());
            DataOutputStream dos = new DataOutputStream(socket.getOutputStream());

//            System.out.print("Message: ");
            StringBuilder message = new StringBuilder();
            StringBuilder tmp = new StringBuilder();

            boolean quitMessage = false;
            while(!quitMessage)
            {
//                System.out.print("Message: ");
                message.replace(0, message.length(), in.nextLine());
                tmp.replace(0, tmp.length(), message.toString().replaceAll("[^a-z|A-Z]", ""));
                quitMessage = tmp.toString().equals("quit");
                dos.writeUTF(message.toString());
                dos.flush();

                int tmpLength = tmp.length();
                for(int i = 0; i < tmpLength; ++i)
                {
                    char c = dis.readChar();
                    System.out.println(c);
                }
            }
            socket.close();
            System.out.println("Client disconnected");
        } catch (IOException e)
        {
            e.printStackTrace();
        }
    }

}
