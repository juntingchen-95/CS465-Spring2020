package a0;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;

public class EchoThread implements Runnable {
    private Socket client;

    // EchoThread constructor.
    public EchoThread(Socket client) {
        this.client = client;
    }

    // Main method of EchoThread Class.
    @Override
    public void run() {
        try {
            // The reader and writer of character stream.
            Reader fromClient = new InputStreamReader(this.client.getInputStream());
            PrintStream toClient = new PrintStream(this.client.getOutputStream());
            char charFromClient;
            // The state machine used to determine quit.
            StateMachine stateMachine = new StateMachine(4);
            // The client loop.
            while (true) {
                // Read character from input stream.
                charFromClient = (char) fromClient.read();
                // Determine whether the input character is an English letter.
                if ((charFromClient >= 65 && charFromClient <= 90)
                        || (charFromClient >= 97 && charFromClient <= 122)) {
                    // Output the character.
                    toClient.print(charFromClient);
                    // Input the character into the state machine.
                    stateMachine.push(charFromClient);
                    // Determine if the input characters are "quit".
                    if (stateMachine.checkState("quit")) {
                        // If the input characters are "quit", break the loop.
                        break;
                    }
                }
            }
            // Close the input and output stream. Finally, close the client thread.
            client.shutdownInput();
            client.shutdownOutput();
            System.out.println("Client (" + client.getInetAddress().getHostAddress() + ") connection terminated.");
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                client.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // The state machine class.
    private static class StateMachine {
        // Define the queue and the queue length.
        private int queueLength;
        private ArrayList<Character> queue;

        // The constructor of the class.
        public StateMachine(int length) {
            queueLength = length;
            queue = new ArrayList<>();
        }

        // Push the character into the queue.
        public void push(char charFromClient) {
            queue.add(charFromClient);
            // If the length of queue greater than the defined value, delete the header of the queue.
            if (queue.size() > queueLength) {
                queue.remove(0);
            }
        }

        // The method to check whether the current queue equal to the parameter string.
        public boolean checkState(String stateString) {
            // Convert the input string to the character array.
            char[] stateStringArray = stateString.toCharArray();
            // Check whether the character array equal to the queue.
            if (queue.size() == queueLength && stateStringArray.length == queueLength) {
                boolean checkFlag = true;
                for (int i = 0; i < queueLength; i++) {
                    if (queue.get(i) != stateStringArray[i]) {
                        checkFlag = false;
                        break;
                    }
                }
                return checkFlag;
            } else {
                return false;
            }
        }
    }
}
