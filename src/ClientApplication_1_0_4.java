import javax.swing.*;
import javax.swing.border.EtchedBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.logging.Logger;
import java.util.List;


public class ClientApplication_1_0_4 extends JFrame {

    private JTextArea chatDisplayArea;
    final private Guest guest;
    final private int offset, portNumber;
    final private String iPAddress;
    private ObjectOutputStream objectOutputStream;

    public ClientApplication_1_0_4(String anIPAddress, int aPortNumber, Guest aGuest, int anOffset) {

        this.iPAddress = anIPAddress;
        this.portNumber = aPortNumber;
        this.guest = aGuest;
        this.offset = anOffset;

        initGUI();
        connect();
    }

    /**
     * This method creates a Socket that connects to the server. If IOException (ConnectException) is thrown
     * it displays a message to the user that it was unable to connect.
     */
    private void connect() {

        try {

            final Socket clientSocket = new Socket(iPAddress, portNumber);
            this.objectOutputStream = new ObjectOutputStream((clientSocket.getOutputStream()));

            sendCredentials();

            final ReceiverThread receiverThread = new ReceiverThread(new ObjectInputStream(clientSocket.getInputStream()));
            receiverThread.start();


        } catch (IOException ioEx) {

            this.chatDisplayArea.append("Unable to connect to server...");

            ioEx.printStackTrace();
        }
    }

    /**
     *
     * This method sends a client's / guests' information to the server.
     * @throws IOException Exception is caught in connect().
     */
    private void sendCredentials()  throws IOException {

        objectOutputStream.writeObject(guest);
        objectOutputStream.flush();

    }

    /**
     * This method creates the GUI for the ClientApplication.
     */
    private void initGUI() {

        this.setResizable(false);

        this.setTitle("Client ID: " + guest.getName());

        this.getContentPane().add(createChatDisplayPanel(), BorderLayout.CENTER);
        this.getContentPane().add(createGuestListPanel(), BorderLayout.LINE_START);
        this.getContentPane().add(createChatInputPanel(), BorderLayout.PAGE_END);

        this.pack();

        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        this.setLocation((int) (GraphicsEnvironment.getLocalGraphicsEnvironment().getCenterPoint().getX()) + offset, (int) GraphicsEnvironment.getLocalGraphicsEnvironment().getCenterPoint().getY());

        this.setVisible(true);
    }

    private JPanel createChatInputPanel() {

        JPanel chatInputPanel = new JPanel(new BorderLayout());

        JLabel inputLabel = new JLabel("Input: ");

        JTextField chatInputField = new JTextField();
        chatInputField.hasFocus();
        chatInputField.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent event) {
                final String outgoingMessage = chatInputField.getText();

                try {
                    objectOutputStream.writeObject(outgoingMessage);
                    objectOutputStream.flush();
                    chatInputField.setText("");
                } catch (IOException e) {
                    e.printStackTrace(); }
            }
        });

        chatInputPanel.add(inputLabel, BorderLayout.LINE_START);
        chatInputPanel.add(chatInputField, BorderLayout.CENTER);

        return chatInputPanel;
    }



    private JPanel createGuestListPanel() {

        final int width = 100, height = 100;

        JPanel guestListPanel = new JPanel(new BorderLayout());

        DefaultListModel<Guest> listModel = new DefaultListModel<>();

        JList<Guest> guestList = new JList<>(listModel);

        JScrollPane guestPane = new JScrollPane(guestList);

        guestPane.setPreferredSize(new Dimension(width, height));

        JLabel guestListLabel = new JLabel("Guests:");

        guestListPanel.add(guestListLabel, BorderLayout.PAGE_START);
        guestListPanel.add(guestPane, BorderLayout.CENTER);

        return guestListPanel;
    }


    private JPanel createChatDisplayPanel() {

        final int rows = 30, columns = 30;

        JPanel chatDisplayPanel = new JPanel(new BorderLayout());

        JLabel chatLabel = new JLabel("Chat:");

        chatDisplayArea = new JTextArea(rows, columns);
        chatDisplayArea.setEditable(false);
        chatDisplayArea.setLineWrap(true);
        chatDisplayArea.setBorder(new EtchedBorder());

        chatDisplayPanel.add(chatLabel, BorderLayout.PAGE_START);
        chatDisplayPanel.add(chatDisplayArea, BorderLayout.CENTER);

        return chatDisplayPanel;
    }

    class ReceiverThread extends Thread {

        final private ObjectInputStream objectInputStream;

        private ReceiverThread(ObjectInputStream anObjectInputStream) {

            this.objectInputStream = anObjectInputStream;
        }

        public void run() {

            while (true) {

                try {

                    Object object = objectInputStream.readObject();

                    if (object instanceof String) {
                        Logger.getGlobal().info("String received.");

                        final String incomingMessage = (String) object;
                        chatDisplayArea.append(incomingMessage);
                    } else if (object instanceof List){


                        Logger.getGlobal().info("Received " + object.getClass().getName());
                    }

                } catch (Exception e) { // What to do here?
                    e.printStackTrace();
                }
            }
        }
    }
}
