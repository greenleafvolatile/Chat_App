import java.util.ArrayList;
import javax.swing.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.ObjectInputStream;
import java.util.logging.Logger;
import java.awt.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

public class ChatFrame extends JFrame implements Runnable {

    private final Guest guest;
    private final ArrayList<Guest> guests;
    private JTextArea textArea;
    private DefaultListModel<Guest> lm;
    private JList<Guest> guestList;
    private final Socket clientSocket;
    private int offset;

    public ChatFrame(int offset, Guest aGuest, Socket aSocket) {
        super("Guest: " + aGuest.getName());
        this.guests = new ArrayList<>();
        this.offset = offset;
        this.guest = aGuest;
        this.clientSocket = aSocket;
        this.initGUI();
    }

    private void initGUI() {

        textArea = new JTextArea(15, 40);
        textArea.setEditable(false);

        lm = new DefaultListModel<>();
        guestList = new JList<>(lm); // You can use setFixedCellWidth() to set the width of a Jlist.

        JScrollPane scrollPane = new JScrollPane(guestList);

        JTextField inputField = new JTextField( 5);

        inputField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {



            }



        });

        this.add(textArea, BorderLayout.CENTER);
        this.add(inputField, BorderLayout.PAGE_END);
        this.add(scrollPane, BorderLayout.LINE_START);

        this.pack();
        inputField.requestFocusInWindow();
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setLocation(offset, 300);
        this.setVisible(true);

        Thread thread = new Thread(this);
        thread.start();
    }

    public void run() {

        BufferedReader in;
        ObjectInputStream objIn;

        try {
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            objIn = new ObjectInputStream(clientSocket.getInputStream());

            String message = in.readLine();
            textArea.append(message);
            Object object = objIn.readObject();
            while(object != null) {
                if (object instanceof Guest) {
                    guests.add((Guest) object);
                }
                object = objIn.readObject();
            }
            updateGuestList();



        }
        catch(IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }

    }

    private void updateGuestList() {

    }
}
