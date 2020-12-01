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
 * 3.
 */
public class ServerApplication_1_0_6 {


    final List<ConnectionHandler> connectionHandlers = new ArrayList<>();
    final Set<Client> clientSet = Collections.synchronizedSet(new HashSet<Client>()); // Perhaps use HashSet instead seeing that all clients should be unique?

    private ServerSocket serverSocket;

    private  boolean isRunning;

    public ServerApplication_1_0_6() {

        try {

            this.serverSocket = new ServerSocket(45369);
            isRunning = true;
            this.init();

        } catch (IOException iOEx) {

            iOEx.printStackTrace();

            // Do something when socket can not be opened.
        }
    }

    private void init() throws IOException {

        System.out.println("The server is awaiting connections.");

        while (isRunning) {

            Socket socket = this.serverSocket.accept();

            System.out.println("A client has connected...");

            ConnectionHandler connectionHandler = new ConnectionHandler(socket);

            this.connectionHandlers.add(connectionHandler);

            connectionHandler.start();

        }

    }

    public static void main(String[] args) {

        new ServerApplication_1_0_6();

    }

    class ConnectionHandler extends Thread {

        final private String clientIPAddress;
        final private ObjectInputStream objectInputStream;
        final private ObjectOutputStream objectOutputStream;
        private Client client;
        private boolean isConnected;

        private ConnectionHandler(Socket socket) throws IOException {

            this.clientIPAddress = socket.getInetAddress().toString();
            this.objectInputStream = new ObjectInputStream(socket.getInputStream());
            this.objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
            this.isConnected = true;
            this.sendConnectMessage();

        }

        private void sendConnectMessage() throws IOException {

            String newLine = System.lineSeparator();
            objectOutputStream.writeObject(new Message("You are connected to the server..." + newLine + newLine, "Admin", null));
            objectOutputStream.flush();

        }

        @Override
        public void run() {

            try {

                while (isConnected) {

                    Object object = objectInputStream.readObject();

                    Logger.getGlobal().info("Server received an object.");

                    if (object instanceof Client) {

                        this.client = (Client) object;
                        this.client.setIPAddress(this.clientIPAddress);

                        ServerApplication_1_0_6.this.clientSet.add(client);

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

                ex.printStackTrace();

                if (ex.getClass() == EOFException.class) { // When client closes socket, that causes EOFException.


                    clientSet.removeIf(client -> client.equals(this.client));

                    isConnected = false;

                    this.updateClientList();

                }

            } catch (ClassNotFoundException cnfEx) {

                cnfEx.printStackTrace();

            } finally {

                try {

                    this.objectOutputStream.close();

                    Logger.getGlobal().info("Stream was closed!");

                } catch (IOException iOEx) {

                    iOEx.printStackTrace();
                }
            }
        }

        private ObjectOutputStream getObjectOutputStream () {

            return this.objectOutputStream;

        }


        private void updateClientList() {

            for (ConnectionHandler connectionHandler : connectionHandlers) {

                if (connectionHandler.isConnected) {

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
}
