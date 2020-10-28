import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.logging.Logger;

public class ClientApplication_1_0_4 extends JFrame {

    final private Socket serverSocket;
    private JTextArea chatDisplayArea;
    final private Guest guest;
    final private int offset;
    final private ObjectOutputStream objectOutputStream;

    public ClientApplication_1_0_4(String iPAddress, int portNumber, Guest aGuest, int anOffset) throws IOException {

        this.serverSocket = new Socket(iPAddress, portNumber);
        this.guest = aGuest;
        this.offset = anOffset;
        this.objectOutputStream = new ObjectOutputStream(serverSocket.getOutputStream());

        sendCredentials();
        initGUI();

        final ReceiverThread receiverThread = new ReceiverThread(new ObjectInputStream(serverSocket.getInputStream()));
        receiverThread.start();
    }

    private void sendCredentials() {

        try {

            objectOutputStream.writeObject(guest);
            objectOutputStream.flush();

        } catch (IOException iOEx) {

            iOEx.printStackTrace();

        }
    }

    private void initGUI() throws IOException {

        final int rows = 30, columns = 30;

        //final ObjectOutputStream objectOutputStream = new ObjectOutputStream(serverSocket.getOutputStream());

        this.setTitle("Client ID: " + guest.getName());

        JTextField chatInputField = new JTextField(columns);
        chatInputField.hasFocus();
        chatInputField.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent event) {
                final String outgoingMessage = chatInputField.getText();

                try {
                    objectOutputStream.writeObject(outgoingMessage);
                    Logger.getGlobal().info("Sent");
                    objectOutputStream.flush();
                    chatInputField.setText("");
                } catch (IOException e) {
                    e.printStackTrace(); }
            }
        });

        chatDisplayArea =  new JTextArea(rows, columns);
        chatDisplayArea.setEditable(false);
        chatDisplayArea.setLineWrap(true);

        this.getContentPane().add(chatInputField, BorderLayout.PAGE_END);
        this.getContentPane().add(chatDisplayArea, BorderLayout.CENTER);

        this.pack();

        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        this.setLocation((int) (GraphicsEnvironment.getLocalGraphicsEnvironment().getCenterPoint().getX()) + offset, (int) GraphicsEnvironment.getLocalGraphicsEnvironment().getCenterPoint().getY());

        this.setVisible(true);
    }

    class ReceiverThread extends Thread {

        final private ObjectInputStream objectInputStream;

        private ReceiverThread(ObjectInputStream anObjectInputStream) {

            this.objectInputStream = anObjectInputStream;
        }

        public void run() {

            while (true) {

                try {

                    //while (objectInputStream.available() > 0) {

                        Logger.getGlobal().info("Test");
                        final String incomingMessage = (String) objectInputStream.readObject();
                        chatDisplayArea.append(incomingMessage);
                    //}


                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
