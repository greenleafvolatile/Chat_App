import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

public class Guest implements Serializable {

    private static final long serialVersionUID = 1L;

    private final String name;
    private final String guestIPAddress;


    public Guest(String aName) {
        InetAddress inetAddress = null;
        try {
            inetAddress = InetAddress.getLocalHost();
        }
        catch (UnknownHostException uHEx) {
            uHEx.printStackTrace();
        }

        this.name = aName;
        assert inetAddress != null : "inetaddress is null.";
        this.guestIPAddress = inetAddress.getHostAddress();
    }

    public String getName() {
        return this.name;
    }



}
