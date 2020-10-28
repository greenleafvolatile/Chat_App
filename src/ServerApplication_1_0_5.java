import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

public class ServerApplication_1_0_5 {

    final List<ConnectionHandler> connectionHandlers = Collections.synchronizedList(new ArrayList<ConnectionHandler>());
    final List<Guest> guests = Collections.synchronizedList(new ArrayList<Guest>());

    final private ServerSocket serverSocket;


    public ServerApplication_1_0_5() throws IOException {

        this.serverSocket = new ServerSocket(45369);
        this.init();

    }

    private void init() throws IOException {

        System.out.println("The server is awaiting connections.");

        while (true) {

            Socket clientSocket = this.serverSocket.accept();

            System.out.println("A client has connnected.");

            ConnectionHandler connectionHandler = new ConnectionHandler(clientSocket);

            connectionHandler.start();

            this.connectionHandlers.add(connectionHandler);

            Logger.getGlobal().info("Number of connection handlers: " + connectionHandlers.size());

        }

    }

    public static void main(String[] args) throws Exception {

        ServerApplication_1_0_5 server = new ServerApplication_1_0_5();
    }

    class ConnectionHandler extends Thread {

        final private ObjectInputStream objectInputStream;
        final private ObjectOutputStream objectOutputStream;

        private ConnectionHandler(Socket clientSocket) throws IOException {

            this.objectInputStream = new ObjectInputStream(clientSocket.getInputStream());
            this.objectOutputStream = new ObjectOutputStream(clientSocket.getOutputStream());
            this.sendConnectMessage();

        }

        private void sendConnectMessage() throws IOException {
            objectOutputStream.writeObject("You are connected to the server...\n\n");
            objectOutputStream.flush();
        }

        @Override
        public void run() {

            while (true) {

                try {

                    Object object = objectInputStream.readObject();

                    if (object instanceof Guest) {

                        guests.add((Guest) object);

                    } else {

                        Logger.getGlobal().info("Check!");
                        String receivedMessage = (String) object;

                        Logger.getGlobal().info("It is a string");

                        for (ConnectionHandler connectionHandler : connectionHandlers) {
                            if (connectionHandler != ConnectionHandler.this) {
                                connectionHandler.objectOutputStream.writeObject(receivedMessage + "\n");
                                Logger.getGlobal().info("Wrote object!");
                                objectOutputStream.flush();
                            }
                        }
                    }


                } catch (IOException | ClassNotFoundException ex) {
                    ex.printStackTrace();

                }
            }
        }
    }
}
