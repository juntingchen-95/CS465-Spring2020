package Chat;

import java.net.InetAddress;
import java.util.StringTokenizer;

public class Client
{
    private final InetAddress ipAddress;
    private final Integer port;
    private final String alias;


    public Client(InetAddress ipAddress, Integer port, String alias)
    {
        this.ipAddress = ipAddress;
        this.port = port;
        this.alias = alias;
    }


    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Client client = (Client) o;

        if (!ipAddress.equals(client.ipAddress)) return false;
        if (!port.equals(client.port)) return false;
        return alias.equals(client.alias);
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////


    public InetAddress getIpAddress()
    {
        return ipAddress;
    }


    public Integer getPort()
    {
        return port;
    }


    public String getAlias()
    {
        return alias;
    }

}
