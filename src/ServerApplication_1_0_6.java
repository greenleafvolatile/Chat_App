import javax.net.ssl.SSLServerSocket;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.logging.Logger;

/**
 * To do:
 *
 * 1. Consider changing clientSet type to HashSet, seeing that all Clients are (should be) unique.
 * 2. Figure out how I want to handle IOExceptions.
 * 3. Account for:
 *      - Dropped connection (trying to write to socket will cause IOException);
 *      - Open connection that is not used (causes read timeout);
 */

public class ServerApplication_1_0_6 {


    final List<ConnectionHandler> connectionHandlers = new ArrayList<>(); // Is list / ArrayList the right data structure in this case?
    final Set<Client> clientSet = Collections.synchronizedSet(new HashSet<>());
    final int portNumber = 45369;
    final private ServerSocket serverSocket;

    private boolean isRunning;

    public ServerApplication_1_0_6() {

        this.serverSocket = createServerSocket();
        this.init();

    }

    private ServerSocket createServerSocket() {

        try {

            return new ServerSocket(portNumber);

        } catch (IOException iOEx) {

            iOEx.printStackTrace();

            return null; // Change this to something else.
        }
    }



    private void init() {

        if (serverSocket == null) {

            // Do something.

        } else {

            this.isRunning = true;

            System.out.println("The server is awaiting connections.");

            while (isRunning) {

                try  {

                    Socket socket = this.serverSocket.accept();

                    System.out.println("A client has connected...");

                    ConnectionHandler connectionHandler = new ConnectionHandler(socket);

                    this.connectionHandlers.add(connectionHandler);

                    connectionHandler.start();

                } catch (IOException iOEx) {

                    iOEx.printStackTrace();
                }
            }
        }
    }

    public static void main(String[] args) {

        new ServerApplication_1_0_6();

    }

    class ConnectionHandler extends Thread {    // Cave: apparently subclassing Thread class is no longer recommended.
                                                // I should decouple the task from the mechanism (?).
        final private String clientIPAddress;
        final private ObjectInputStream objectInputStream;
        final private ObjectOutputStream objectOutputStream;
        final private String newLine;

        private Client client;

        private ConnectionHandler(Socket socket) throws IOException {

            this.clientIPAddress = socket.getInetAddress().toString();

            this.objectInputStream = new ObjectInputStream(socket.getInputStream());

            this.objectOutputStream = new ObjectOutputStream(socket.getOutputStream());

            newLine = System.lineSeparator();

        }

        /* private void sendConnectedMessage() throws IOException {

            objectOutputStream.writeObject(new Message("You are connected to the server..." + newLine, "Admin", null));
            objectOutputStream.flush();
        } */

        @Override
        public void run() {


            try {

                while (true) {

                    Object object = objectInputStream.readObject();

                    if (object instanceof Client) {

                        this.client = (Client) object;
                        this.client.setIPAddress(this.clientIPAddress);

                        ServerApplication_1_0_6.this.clientSet.add(client);

                        this.sendConnectedMessage();

                        this.updateClientList();


                    } else if (object instanceof Message) {

                        Message incomingMessage = (Message) object;

                        if (incomingMessage.getReceiver().equals("All")) {

                            for (ConnectionHandler connectionHandler : connectionHandlers) {

                                if (connectionHandler != ConnectionHandler.this) {

                                    connectionHandler.getObjectOutputStream().writeObject(object);

                                    connectionHandler.getObjectOutputStream().flush();
                                }
                            }
                        }
                    }
                }

            } catch (IOException ex) {

                Logger.getGlobal().info("IOException thrown.");

                ex.printStackTrace();

                if (ex.getClass() == EOFException.class) { // When client socket is closed, that causes EOFException.

                    System.out.println("A client has disconnected.");

                    clientSet.removeIf(client -> client.equals(this.client));

                    ServerApplication_1_0_6.this.connectionHandlers.remove(ConnectionHandler.this);
                    //this.isConnected = false;

                    this.sendDisconnectedMessage();

                    this.updateClientList();

                }

            } catch (ClassNotFoundException cnfEx) {

                cnfEx.printStackTrace();

            } finally {

                try {

                    this.objectOutputStream.close();

                    Logger.getGlobal().info("Output stream was closed.");

                } catch (IOException iOEx) {

                    iOEx.printStackTrace();
                }
            }
        }

        private ObjectOutputStream getObjectOutputStream () {

            return this.objectOutputStream;

        }


        private void sendConnectedMessage() throws IOException {

            for (ConnectionHandler connectionHandler : connectionHandlers) {

                if (connectionHandler == this) {

                    connectionHandler.objectOutputStream.writeObject(new Message("You are connected to the server..." + newLine, "Admin", null));

                } else {

                    connectionHandler.objectOutputStream.writeObject(new Message(this.client.getName() + " has connected to the server..." + newLine, "Admin", null));
                }

                connectionHandler.objectOutputStream.flush();
            }
        }

        private void sendDisconnectedMessage() {

            for (ConnectionHandler connectionHandler : connectionHandlers) {

                try {

                    connectionHandler.objectOutputStream.writeObject(new Message(String.format("%s has disconnected." + newLine, this.client.getName()), "Admin", null));
                    connectionHandler.objectOutputStream.flush();

                } catch (IOException iOEx) {

                    iOEx.printStackTrace();
                }
            }
        }


        private void updateClientList() {

            for (ConnectionHandler connectionHandler : connectionHandlers) {


                try {

                    connectionHandler.getObjectOutputStream().writeObject(ServerApplication_1_0_6.this.clientSet);

                    connectionHandler.getObjectOutputStream().flush(); // Move outside if - else-if block?

                    connectionHandler.getObjectOutputStream().reset();

                } catch (IOException iOEx) {

                    // Do something here.

                    iOEx.printStackTrace();

                }
            }
        }
    }
}
