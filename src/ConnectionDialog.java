import java.io.IOException;
import java.net.Socket;
import java.util.logging.Logger;
import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.ObjectOutputStream;


public class ConnectionDialog extends JDialog implements Runnable {


    private static final int PORT_NUMBER = 45369;
    private volatile Socket socket; // Use volatile for this attribute?
    private Guest guest;
    private int offset;

    public ConnectionDialog(int offset) {
        this.offset=offset;
        initGUI();
    }

    private void initGUI() {
        JPanel panel = new JPanel();

        JLabel nameLabel = new JLabel("Enter your name:");
        panel.add(nameLabel);

        final int textFieldWidth = 15;
        JTextField nameField = new JTextField(textFieldWidth);
        panel.add(nameField);

        final int nrOfButtons = 1;
        JButton connectButton = new JButton("Connect");
        connectButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent event) {
                ConnectionDialog.this.guest = new Guest(nameField.getText());

                ConnectionDialog.this.connect();
                ConnectionDialog.this.sendCredentials();

                new ChatFrame(ConnectionDialog.this.offset, ConnectionDialog.this.guest, ConnectionDialog.this.socket);
                ConnectionDialog.this.dispose();
            }
        });

        JButton[] buttonArray = new JButton[nrOfButtons];
        buttonArray[0] = connectButton;

        JOptionPane pane = new JOptionPane(panel, JOptionPane.PLAIN_MESSAGE, JOptionPane.DEFAULT_OPTION, null, buttonArray, buttonArray[0]);

        this.add(pane);
        this.pack();
        this.setLocation(offset, 300);
        this.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        this.setVisible(true);
    }

    public void run(){
        connect();
        sendCredentials();
    }

    public void connect() {
        socket = null;
        try {
            socket = new Socket("localhost", PORT_NUMBER);
        }
        catch(IOException ioEx) {
            JOptionPane.showMessageDialog(this, "Error: " + ioEx.getMessage());
            System.exit(0);
        }
    }

    public void sendCredentials() {
        ObjectOutputStream out;
        try {
            out = new ObjectOutputStream(socket.getOutputStream());
            out.writeObject(guest);
        }
        catch(IOException ex) {
            ex.printStackTrace();
        }
    }


    public static void main(String[] args) {
        try {
            String lfClassName = UIManager.getCrossPlatformLookAndFeelClassName();
            UIManager.setLookAndFeel(lfClassName);
        } catch (IllegalAccessException | InstantiationException | UnsupportedLookAndFeelException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                new ConnectionDialog(100);
                new ConnectionDialog(600);
            }
        });
    }
}
