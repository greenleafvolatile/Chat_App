import java.io.*;
import java.util.logging.Logger;

public class Client implements Serializable {

    private static final long serialVersionUID = 1L;

    private final String name;
    private final String nickname;
    private final String emailAddress;
    private String iPAddress;

    public Client(String aName, String aNickname, String anEmailAddress) {

        this.name = aName;
        this.nickname = aNickname;
        this.emailAddress= anEmailAddress;
    }

    public int hashCode() {

        final int prime = 31;
        int hash = 7;
        hash = prime * hash + (this.name == null ? 0 : this.name.hashCode());
        hash = prime * hash + (this.nickname == null ? 0 : this.nickname.hashCode());
        hash = prime * hash + (this.emailAddress == null ? 0 : this.emailAddress.hashCode());
        // Logger.getGlobal().info("Hash: " + hash);
        return hash;

    }

    /**
     * Note: not accounting for nulls, because ConnectionDialog will not allow nulls for name, nickname and email-address fields (WIP).
     * @param object
     * @return
     */
    public boolean equals (final Object object) {

        if (this == object) {

            return true;
        }

        if (object == null) {

            return false;
        }

        if (this.getClass() != object.getClass()) {

            return false;
        }

        final Client otherClient = (Client) object;

        return this.name.equals(otherClient.name) && this.nickname.equals(otherClient.nickname) && this.emailAddress.equals(otherClient.emailAddress);
    }



    public String getName() {

        return this.name;

    }

    public String getNickname() {

        return this.nickname;

    }

    public String getEmailAddress() {

        return this.emailAddress;

    }

    public String getiPAddress() {

        return this.iPAddress;

    }

    public void setIPAddress(String anIPAddress) {

        this.iPAddress = anIPAddress;
    }
}


/* To do:

1) What to do with UnknownHostException? Where to catch?

*/
