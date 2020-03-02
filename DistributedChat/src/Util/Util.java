package Util;

import java.net.*;
import java.util.Enumeration;
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
    public final static String listCommand = "/list";     // Command to list the available network interfaces of the user


    /**
     * Function to display to the user the different available commands he can use. Update this function when adding
     * new commands to the application.
     */
    public static void displayHelpCommand()
    {
        // To update when adding new commands
        System.out.println("Available commands: ");
        System.out.println(helpCommand);
        System.out.println("   Display the help");
        System.out.println(joinCommand + " ipAddress port");
        System.out.println("   Join an existing chat given the IP address and the port of an already connected user");
        System.out.println("   ipAddress is a correctly formatted IPv4 address (xxx.xxx.xxx.xxx, xxx between 0 and 255)");
        System.out.println("   port is an integer between 0 and 65,536");
        System.out.println(startCommand);
        System.out.println("   Start a new chat on your machine");
        System.out.println(listCommand);
        System.out.println("   List the available network interfaces for you to start a server on");
        System.out.println(quitCommand);
        System.out.println("   Quit the current chat room, or quit the application if not connected to any chat");
    }


    /**
     * List all the available network interfaces of the machine. Mostly used to inform the user on which interface
     * a new chat can possibly be started.
     */
    public static void listNetworkInterfaces()
    {
        System.out.println("Available network interfaces on this machine: ");
        // Retrieve the different available network interfaces
        Enumeration<NetworkInterface> networkInterfaces = null;
        try
        {
            networkInterfaces = NetworkInterface.getNetworkInterfaces();
        } catch (SocketException e) {
            throw new RuntimeException(e);
        }

        // If one or more network interfaces have been retrieved
        if (null == networkInterfaces)
        {
            System.out.println("   Could not find any available network interface");
        } else {
            while (networkInterfaces.hasMoreElements())
            {
                NetworkInterface element = networkInterfaces.nextElement();

                // Discard virtual network interfaces
                if (element.isVirtual()){
                    continue;
                }

                // Only keep wifi, ethernet and localhost interfaces, and display their information
                Enumeration<InetAddress> addresses = element.getInetAddresses();
                while (addresses.hasMoreElements())
                {
                    InetAddress ip = addresses.nextElement();
                    if (ip instanceof Inet4Address)
                    {
                        System.out.println("   " + element.getDisplayName() + " - " + ip.getHostAddress());
                    }
                }
            }
        }
    }


    /**
     * Check if the IP address is a correctly formatted IPv4 address xxx.xxx.xxx.xxx, where xxx must be positive and not
     * greater than 255.
     * @param ipAddress String representing the IPv4 address to check.
     * @return True if the IP address is correctly formatted, false otherwise.
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
     * @return True if port is positive and not greater than 65,535.
     */
    public static boolean checkPort(int port)
    {
        return (port >= 1 && port <= 65535);
    }
}
