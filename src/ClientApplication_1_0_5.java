import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.text.BadLocationException;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import java.awt.*;
import java.awt.event.*;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.IOException;
import java.net.ConnectException;
import java.net.Socket;
import java.net.SocketException;
import java.util.Set;
import java.util.logging.Logger;
import java.util.List;


/**
 ** The ClientApplication is a GUI application that can be used by a user to
 * communicate with other users of the same ClientApplication via a server.
 *
 * N.B. Inner class(es) at the  bottom.
 *
 * To-do:
 *
 * 1. Properly handle exceptions.
 * 2. Refactor Client to be named Guest?
 * 3. Make clientSocket a private final instance variable.
 *
 */

public class ClientApplication_1_0_5 extends JFrame {

    private JTextPane chatDisplayArea;
    private StyledDocument styledDocument;
    private SimpleAttributeSet boldText, redText;
    private JList<Client> guestList;
    private final Client client;
    private final int offset, portNumber;
    private final String iPAddress;
    private Socket clientSocket;
    private String lineFeed;
    private DefaultListModel<Client> listModel;
    private ObjectOutputStream objectOutputStream;
    private boolean isConnected;

    public ClientApplication_1_0_5(String anIPAddress, int aPortNumber, Client aClient, int anOffset) {


        this.iPAddress = anIPAddress;
        this.portNumber = aPortNumber;
        this.client = aClient;
        this.offset = anOffset;
        this.lineFeed = System.lineSeparator();

        initGUI();
        connect();
    }

    /**
     * This method creates a Socket that connects to the server. If IOException (ConnectException) is thrown
     * it displays a message to the user that it was unable to connect.
     */
    private void connect() {

        try {

            this.clientSocket = new Socket(iPAddress, portNumber);

            this.objectOutputStream = new ObjectOutputStream((clientSocket.getOutputStream()));

            isConnected = true;

            sendCredentials();

            final ReceiverThread receiverThread = new ReceiverThread(new ObjectInputStream(clientSocket.getInputStream()));
            receiverThread.start();


        } catch (IOException ioEx) {

            if (ioEx instanceof ConnectException) {

                try {

                    this.styledDocument.insertString(styledDocument.getLength(), "Unable to connect to server at:\n IP: " + this.iPAddress + "\nPort: " + this.portNumber, redText);

                } catch (BadLocationException bLE) {

                    bLE.printStackTrace();

                }

            }

            ioEx.printStackTrace();
        }
    }

    /**
     * This method sends a client's / guests' information to the server.
     *
     * @throws IOException Exception is caught in connect().
     */
    private void sendCredentials() throws IOException { // Should this even be a separate method?

        objectOutputStream.writeObject(client);
        objectOutputStream.flush();

    }

    /**
     * This method creates the GUI for the ClientApplication.
     */
    private void initGUI() {

        this.setResizable(false);

        this.setTitle("Client ID: " + client.getName());

        this.getContentPane().add(createChatDisplayPanel(), BorderLayout.CENTER);
        this.getContentPane().add(createGuestListPanel(), BorderLayout.LINE_START);
        this.getContentPane().add(createChatInputPanel(), BorderLayout.PAGE_END);
        this.setJMenuBar(createMenuBar());

        this.pack();

        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        this.setLocation((int) (GraphicsEnvironment.getLocalGraphicsEnvironment().getCenterPoint().getX()) + offset, (int) GraphicsEnvironment.getLocalGraphicsEnvironment().getCenterPoint().getY());

        this.setVisible(true);
    }

    private JMenuBar createMenuBar() {

        JMenuItem closeItem = new JMenuItem("Disconnect");
        closeItem.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent event) {

                try {

                    isConnected = false;

                    objectOutputStream.close(); // per stackoverflow: you should close the outermost OutputStream yourself, otherwise it will not get flushed and you can lose data.
                                                // Closing the stream also closes the socket.

                } catch (IOException ioEx) {

                    ioEx.printStackTrace(); // How to handle?

                }

                new ConnectionDialog_1_0_1(ClientApplication_1_0_5.this.offset);


                ClientApplication_1_0_5.this.dispose();

            }
        });

        JMenu optionsMenu = new JMenu("Options");
        optionsMenu.add(closeItem);

        JMenuBar menuBar = new JMenuBar();
        menuBar.add(optionsMenu);

        return menuBar;
    }

    private JPanel createChatInputPanel() {

        JPanel chatInputPanel = new JPanel(new BorderLayout());
        chatInputPanel.setBorder(new EtchedBorder(EtchedBorder.RAISED));

        JLabel inputLabel = new JLabel("Input: ");

        JTextField chatInputField = new JTextField();
        chatInputField.hasFocus();
        chatInputField.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent event) {

                final String outgoingMessage = chatInputField.getText();

                try {

                    objectOutputStream.writeObject(new Message(outgoingMessage, ClientApplication_1_0_5.this.client.getName(), "All"));
                    objectOutputStream.flush();

                    chatInputField.setText("");

                    try {

                        styledDocument.insertString(styledDocument.getLength(),"You say: ", boldText);
                        styledDocument.insertString(styledDocument.getLength(), outgoingMessage + lineFeed, null);

                    } catch (BadLocationException badEx) {

                        badEx.printStackTrace();

                    }


                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        chatInputPanel.add(inputLabel, BorderLayout.LINE_START);
        chatInputPanel.add(chatInputField, BorderLayout.CENTER);

        return chatInputPanel;
    }


    private JPanel createGuestListPanel() {

        final int width = 100, height = 100;

        JPanel guestListPanel = new JPanel(new BorderLayout());
        guestListPanel.setBorder(new EtchedBorder(EtchedBorder.RAISED));

        listModel = new DefaultListModel<>();

        guestList = new JList<>(listModel);
        guestList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        guestList.setCellRenderer(new GuestListRenderer());
        guestList.addMouseListener(new ListItemListener());

        JScrollPane guestPane = new JScrollPane(guestList); // Set to always show horizontal scrollbar?
        guestPane.setPreferredSize(new Dimension(width, height));
        guestPane.setBorder(new EtchedBorder(EtchedBorder.LOWERED));

        JLabel guestListLabel = new JLabel("Guests:");

        guestListPanel.add(guestListLabel, BorderLayout.PAGE_START);
        guestListPanel.add(guestPane, BorderLayout.CENTER);

        return guestListPanel;
    }

    private JPanel createChatDisplayPanel() {

        final float lineSpacing = 5.0f;

        JPanel chatDisplayPanel = new JPanel(new BorderLayout());
        chatDisplayPanel.setBorder(new EtchedBorder(EtchedBorder.RAISED));

        JLabel chatLabel = new JLabel("Chat:");

        chatDisplayArea = new JTextPane();
        chatDisplayArea.setEditable(false);
        chatDisplayArea.setContentType("text");
        chatDisplayArea.setPreferredSize(new Dimension(200, 400));
        chatDisplayArea.setBorder(new EtchedBorder(EtchedBorder.LOWERED));

        styledDocument = chatDisplayArea.getStyledDocument();

        SimpleAttributeSet spaceBelow = new SimpleAttributeSet();

        StyleConstants.setSpaceBelow(spaceBelow, lineSpacing);
        styledDocument.setParagraphAttributes(0, styledDocument.getLength(), spaceBelow, false);

        boldText= new SimpleAttributeSet();
        StyleConstants.setBold(boldText, true);

        redText = new SimpleAttributeSet();
        StyleConstants.setForeground(redText, Color.RED);

        chatDisplayPanel.add(chatLabel, BorderLayout.PAGE_START);
        chatDisplayPanel.add(chatDisplayArea, BorderLayout.CENTER);

        return chatDisplayPanel;
    }

    class ReceiverThread extends Thread {

        final private ObjectInputStream objectInputStream;

        private ReceiverThread(ObjectInputStream anObjectInputStream) {

            this.objectInputStream = anObjectInputStream;
        }

        @SuppressWarnings("unchecked") // Should I do this? Am I doing this correctly?
        public void run() {

            while (isConnected) {

                try {

                    Object object = objectInputStream.readObject();

                    if (object instanceof Message) {

                        Message incomingMessage = (Message) object;

                        if (!incomingMessage.getSender().equals("Admin")) {

                            ClientApplication_1_0_5.this.styledDocument.insertString(styledDocument.getLength(), incomingMessage.getSender() + " says: ", boldText);
                            ClientApplication_1_0_5.this.styledDocument.insertString(styledDocument.getLength(), incomingMessage.getContent() + lineFeed , null);

                        } else {

                            ClientApplication_1_0_5.this.styledDocument.insertString(0, incomingMessage.getContent(), redText);

                        }


                    } else if (object instanceof Set) {


                        final Set<Client> clientSet = (Set<Client>) object; // This line causes IntelliJ to complain about 'unchecked cast'.
                        Logger.getGlobal().info("Set size: " + clientSet.size());


                        ClientApplication_1_0_5.this.listModel.removeAllElements();

                        for (Object anObject : clientSet) {

                            if  (!((Client) anObject).getName().equals(ClientApplication_1_0_5.this.client.getName())) {

                                ClientApplication_1_0_5.this.listModel.addElement((Client) anObject);
                            }
                        }
                    }

                } catch (IOException ioEx) {

                    ioEx.printStackTrace();

                } catch (BadLocationException badEx) {

                    badEx.printStackTrace();

                } catch (ClassNotFoundException cNFEx) {

                    cNFEx.printStackTrace();
                }

            }
        }
    }

    private class GuestListRenderer extends JLabel implements ListCellRenderer<Client> {

        private GuestListRenderer() {

            this.setOpaque(true);
            this.setHorizontalAlignment(SwingConstants.LEFT);
            this.setVerticalAlignment(SwingConstants.CENTER);
            this.setBorder(new EmptyBorder(0, 0, 5,0));

        }

        public Component getListCellRendererComponent(JList list, Client aClient, int index, boolean isSelected, boolean cellHasFocus) {

            this.setText(aClient.getName());

            if (isSelected) {

                this.setBackground(list.getSelectionBackground());
                this.setForeground(list.getSelectionForeground());

            } else {

                this.setBackground(list.getBackground());
                this.setForeground(list.getForeground());

            }
            return this;
        }
    }

    private class ListItemListener extends MouseAdapter {

        private ListItemListener() {

        }

        @Override
        public void mousePressed(MouseEvent mE) {
            maybeShowPopUp(mE);
        }

        @Override
        public void mouseReleased(MouseEvent mE) {

            maybeShowPopUp(mE);
        }

        private void maybeShowPopUp(MouseEvent mE){

            int selectedIndex =  guestList.getSelectedIndex();
            Rectangle listCellBounds = guestList.getCellBounds(selectedIndex, selectedIndex);

            if (mE.isPopupTrigger()) {

                if (selectedIndex != -1) {

                    if (listCellBounds.contains(mE.getPoint())) {

                        GuestListRenderer guestListRenderer = (GuestListRenderer) guestList.getCellRenderer();

                        FontMetrics fm = guestListRenderer.getFontMetrics(guestListRenderer.getFont());

                        // To do: list should be populated with nicknames. Change getName() to getNickname().
                        int widthOfTextInCell = fm.stringWidth(listModel.getElementAt(selectedIndex).getName());

                        JPopupMenu popupMenu = createPopUpMenu();

                        popupMenu.show(guestList, (int) (listCellBounds.getMinX() + widthOfTextInCell), (int) listCellBounds.getMaxY());

                    }
                }
            }

        }

        private JPopupMenu createPopUpMenu() {

            JPopupMenu popUpmenu = new JPopupMenu();

            popUpmenu.add(new AbstractAction("Private chat") {

                public void actionPerformed(ActionEvent event) {

                }

            });

            return popUpmenu;
        }

    }





}
