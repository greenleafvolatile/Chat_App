import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.logging.Logger;
import javax.swing.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class Server extends JFrame {

    private static final int SERVER_PORT_NR = 45369;

    // What would be the 'best' data structure here?
    private ArrayList<Guest> guests;

    public Server() {
        guests = new ArrayList<>();
        this.init();
    }

    private void init() {
        //ServerSocket serverSocket = null;

        try (ServerSocket serverSocket = new ServerSocket(SERVER_PORT_NR)) {

            while(true) {

                Socket clientSocket = serverSocket.accept();

                PrintWriter toClient = new PrintWriter(clientSocket.getOutputStream(), true);

                // Construct an ObjectInputStream for receiving Guest objects.
                ObjectInputStream objectInputStream = new ObjectInputStream(clientSocket.getInputStream());
                try {
                    Guest guest = (Guest) objectInputStream.readObject();
                    guests.add(guest);

                    Logger.getGlobal().info("guests array length: " + guests.size());
                    ObjectOutputStream objectOutputStream = new ObjectOutputStream(clientSocket.getOutputStream());
                    for(Guest aGuest : guests) {
                        objectOutputStream.writeObject(aGuest);
                    }
                }
                catch(ClassNotFoundException e) {
                    e.printStackTrace();
                }

                String connectMessage = "Connected to server...";
                toClient.println(connectMessage);

            }

        } catch (IOException ioEx) { // | ClassNotFoundException ioEx) {
            ioEx.printStackTrace();
        }
    }

    public ArrayList<Guest> getGuests() {
        return this.guests;
    }

    public static void main(String[] args) {
        new Server();
    }
}


