import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

// Names
// https://youtu.be/Bm2NhvO4dMY

public class EchoThread_2 implements Runnable
{
    private Thread thread;
    private Socket socket;

    // Creating the socket of the client that is attached to the server
    EchoThread_2(Socket socket)
    {
        this.socket = socket;
    }

    // Function called when starting a new EchoThread which creates a new thread
    public void start()
    {
        if(null == thread)
        {
            thread = new Thread(this, "serverThread");
            thread.start();
        }
    }

    // Function that handles server-client communications
    @Override
    public void run()
    {
        // The two streams used for server-client communications
        DataInputStream fromClient = null;
        DataOutputStream toClient = null;
        try
        {
            // Creating the stream objects from the socket
            fromClient = new DataInputStream(socket.getInputStream());
            toClient = new DataOutputStream(socket.getOutputStream());
        }catch(IOException e)
        {
            e.printStackTrace();
        }

        // Storing four booleans to represent the quit message
        // Act as the state machine
        boolean[] quitMessage = {false, false, false, false};
        boolean clientQuit = false;

        while(!clientQuit)
        {
            // Character to store what the server reads from the client socket
            char charFromClient = '\\';
            try{
                // Reading character by character
                charFromClient = (char) fromClient.readByte();
                // Printing the character to ensure a good reception of this one (only useful for testing)
                System.out.println(charFromClient);
            }catch(IOException e)
            {
                continue;
            }

            // If the character read is a letter (i.e., throw all numbers, special characters, etc.)
            if(Character.isLetter(charFromClient))
            {
                // Switch over the character to determine if it composes the quit message
                // I.e., update the state machine (defined as an array of booleans)
                switch(charFromClient)
                {
                    case 'q':
                        // 'q' is always set to true when read, as "qquit" is valid for example
                        quitMessage[0] = true;
                        break;
                    case 'u':
                        // If the 'u' has not been read yet
                        if (!quitMessage[1])
                        {
                            // But we read a 'q'
                            // 'q' = true, 'u' = true
                            if (quitMessage[0])
                            {
                                quitMessage[1] = true;
                            }
                        }else{
                            // If 'u' has been read already, then the state machine gets to its initial state
                            // "quu" makes the machine to go into this state for example
                            // 'q' = false, 'u' = false;
                            quitMessage[0] = false;
                            quitMessage[1] = false;
                        }
                        break;
                    case 'i':
                        // If 'i' has not been read yet
                        if(!quitMessage[2])
                        {
                            // But we read a 'u' (and therefore a 'q' as well)
                            // Then 'q' = true, 'u' = true and 'i' = true
                            if(quitMessage[1])
                            {
                                quitMessage[2] = true;
                            }
                        }else{
                            // Otherwise, if multiple 'i's are read
                            // Then the state machine goes back into its initial state
                            // 'q' = false, 'u' = false, 'i' = false;
                            quitMessage[0] = false;
                            quitMessage[1] = false;
                            quitMessage[2] = false;
                        }
                        break;
                    case 't':
                        // If we read a 't' and we read a 'i' before
                        // (and therefore a 'u' and a 'q')
                        // Then the state machine goes into its final state
                        // 'q' = true, 'u' = true, 'i' = true and 't' = true
                        // And we set the boolean clientQuit (controling the infinite loop) to true
                        if(quitMessage[2])
                        {
                            quitMessage[3] = true;
                            clientQuit = true;
                        }else{
                            // If we did not read an 'i' before, then the state machine gets back into its
                            // initial state
                            // 'q' = false, 'u' = false
                            quitMessage[0] = false;
                            quitMessage[1] = false;
                        }
                    default:
                        // If we read any other character, the state machine is reset
                        quitMessage[0] = false;
                        quitMessage[1] = false;
                        quitMessage[2] = false;
                        quitMessage[3] = false;
                        break;
                }

                try
                {
                    // The character read from the client is then wrote to the client
                    toClient.writeChar(charFromClient);
                    toClient.flush();
                }catch (IOException e){
                    e.printStackTrace();
                }
            }
        }

        System.out.println("Client disconnected");

        try{
            // Closing the client socket
            socket.close();
        }catch(IOException e){
            e.printStackTrace();
        }
    } // run()

} // EchoThread
