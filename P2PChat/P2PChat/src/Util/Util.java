package Util;

import java.util.Scanner;
import java.util.StringTokenizer;

/**
 * Class to contain utilitarian functions that don't really fit in the main classes of the program or that might be used
 * by several classes. These functions should not be related to any network stuff relative to the chat itself.
 */
public class Util
{
    public final static String quitCommand = "/quit";     // Command to leave the chat
    public final static String helpCommand = "/help";     // Command to display the other commands
    public final static String joinCommand = "/join";     // Command to join an existing chat
    public final static String startCommand = "/start";   // Command to start a new chat

    public static void displayHelpCommand()
    {
        // To update when adding new commands
        System.out.println("Available commands: ");
        System.out.println(Util.helpCommand);
        System.out.println("   Display the help");
        System.out.println(Util.joinCommand + " ipAddress port");
        System.out.println("   Join an existing chat given the IP address and the port of an already connected user");
        System.out.println("   ipAddress is a correctly formated IPv4 address (xxx.xxx.xxx.xxx, xxx between 0 and 255)");
        System.out.println("   port is an integer between 0 and 65,536");
        System.out.println(Util.startCommand + " ipAddress port");
        System.out.println("   Start a new chat on your machine on the given IP address and port");
        System.out.println("   ipAddress is a correctly formated IPv4 address (xxx.xxx.xxx.xxx, xxx between 0 and 255)");
        System.out.println("   port is an integer between 0 and 65,536");
        System.out.println(Util.quitCommand);
        System.out.println("   Quit the current chat room, or quit the application if not connected to any chat");
    }

    /**
     * Check if the IP address is a correctly formated IPv4 address xxx.xxx.xxx.xxx, where xxx must be positive and not
     * greater than 255.
     * @param ipAddress String representing the IPv4 address to check.
     * @return True if the IP address is correctly formated, false otherwise.
     */
    public static boolean checkIPv4Address(String ipAddress)
    {
        StringTokenizer tokenizer = new StringTokenizer(ipAddress, ".");
        if (4 != tokenizer.countTokens())
        {
            return false;
        }
        for (int i = 0; i < 4; i++)
        {
            int token = Integer.parseInt(tokenizer.nextToken());
            if (token < 0 || 255 < token)
            {
                return false;
            }
        }
        return true;
    }


    /**
     * Check if the port is a correct number for a port: positive and not greater than 65,536 (current limit for a port).
     * @param port Integer with the value of the port to check.
     * @return True if port is positive and not greater than 65,536.
     */
    public static boolean checkPort(int port)
    {
        return (port < 0 || 65536 < port);
    }


    /**
     * Function to read a correctly formated IPv4 address from an input. Uses the function checkIPv4Address to check user's input.
     * @param in Scanner to read the address from.
     * @return A correctly formated IPv4 address, following the conditions set by the checkIPv4Address function.
     */
    public static String readIPv4Address(Scanner in)
    {
        String ipAddress = in.nextLine();
        while (!checkIPv4Address(ipAddress))
        {
            System.out.println("IPv4 address incorrectly formated. " +
                    "IPv4 address format: xxx.xxx.xxx.xxx, where xxx is positive and not greater than 255");
            System.out.print("Please enter a valid IPv4 address: ");
            ipAddress = in.nextLine();
        }
        return ipAddress;
    }


    /**
     * Function to read a correct value of port (between 0 and 65,536).
     * @param in Scanner to read the port from.
     * @return A correct port value between 0 and 65,536.
     */
    public static int readPort(Scanner in)
    {
        int port = in.nextInt();
        while(!checkPort(port))
        {
            System.out.println("Incorrect port to connect to. A correct port is positive, and not greater than 65,536.");
            System.out.println("Please enter a valid port: ");
            port = in.nextInt();
        }
        return port;
    }
}
