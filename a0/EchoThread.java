package a0;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;

public class EchoThread implements Runnable {
    private Socket client;

    public EchoThread(Socket client) {
        this.client = client;
    }

    @Override
    public void run() {
        try {
            Reader fromClient = new InputStreamReader(this.client.getInputStream());
            PrintStream toClient = new PrintStream(this.client.getOutputStream());
            char charFromClient;
            StateMachine stateMachine = new StateMachine(4);
            while (true) {
                charFromClient = (char) fromClient.read();
                if ((charFromClient >= 65 && charFromClient <= 90)
                        || (charFromClient >= 97 && charFromClient <= 122)) {
                    toClient.print(charFromClient);
                    stateMachine.push(charFromClient);
                    if (stateMachine.checkState("quit")) {
                        break;
                    }
                }
            }
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

    private static class StateMachine {
        private int queueLength;
        private ArrayList<Character> queue;

        public StateMachine(int length) {
            queueLength = length;
            queue = new ArrayList<>();
        }

        public void push(char charFromClient) {
            queue.add(charFromClient);
            if (queue.size() > queueLength) {
                queue.remove(0);
            }
        }

        public boolean checkState(String stateString) {
            char[] stateStringArray = stateString.toCharArray();
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
