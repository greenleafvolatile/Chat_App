import java.awt.*;
import javax.swing.*;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;


public class ConnectionDialog_1_0_1 extends JDialog {

    private JTextField nameField, nicknameField, emailAddressField, iPField, portField;
    final private int offset;

    public ConnectionDialog_1_0_1(int offset) {
        this.offset = offset;
        initGUI();
    }

    private void initGUI() {

        final int textFieldWidth = 15;

        this.setTitle("Connection dialog");

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.add(createClientInformationPanel(textFieldWidth), BorderLayout.CENTER);
        mainPanel.add(createServerInformationPanel(textFieldWidth), BorderLayout.SOUTH);

        JButton connectButton = new JButton("Connect");

        connectButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent event) {


                Client client = new Client(nameField.getText(), nicknameField.getText(), emailAddressField.getText());

                new ClientApplication_1_0_5("127.0.0.1", 45369, client, ConnectionDialog_1_0_1.this.offset);

                ConnectionDialog_1_0_1.this.dispose();
            }
        });

        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent event) {
                System.exit(0);
            }
        });

        final int nrOfButtons = 2;
        JButton[] buttonArray = new JButton[nrOfButtons];
        buttonArray[0] = connectButton;
        buttonArray[1] = cancelButton;

        JOptionPane pane = new JOptionPane(mainPanel, JOptionPane.PLAIN_MESSAGE, JOptionPane.DEFAULT_OPTION, null, buttonArray, buttonArray[0]);

        this.add(pane);
        this.pack();
        this.setLocation((int) (GraphicsEnvironment.getLocalGraphicsEnvironment().getCenterPoint().getX() + offset), (int) GraphicsEnvironment.getLocalGraphicsEnvironment().getCenterPoint().getY());
        this.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        this.setVisible(true);
    }

    private JPanel createClientInformationPanel(int textFieldWidth) {

        GridBagLayout gridBag = new GridBagLayout();

        JPanel clientInformationPanel = new JPanel();
        TitledBorder titledBorder = new TitledBorder(new EtchedBorder(), "Client Information:");
        clientInformationPanel.setBorder(titledBorder);
        clientInformationPanel.setLayout(gridBag);

        nameField = new JTextField(textFieldWidth);
        nicknameField = new JTextField(textFieldWidth);
        emailAddressField = new JTextField(textFieldWidth);

        JLabel nameLabel = new JLabel("Full Name:");
        nameLabel.setDisplayedMnemonic('F');
        nameLabel.setLabelFor(nameField);

        JLabel nicknameLabel = new JLabel("Nickname:");
        nicknameLabel.setDisplayedMnemonic('N');
        nicknameLabel.setLabelFor(nicknameField);

        JLabel emailAddressLabel = new JLabel("Email-address:");
        emailAddressLabel.setDisplayedMnemonic('E');
        emailAddressLabel.setLabelFor(emailAddressField);

        gridBag.setConstraints(nameLabel, createConstraints(0, 0));
        gridBag.setConstraints(nameField, createConstraints(1, 0));
        gridBag.setConstraints(nicknameLabel, createConstraints(0, 1));
        gridBag.setConstraints(nicknameField, createConstraints(1, 1));
        gridBag.setConstraints(emailAddressLabel, createConstraints(0, 2));
        gridBag.setConstraints(emailAddressField, createConstraints(1, 2));

        clientInformationPanel.add(nameLabel);
        clientInformationPanel.add(nameField);
        clientInformationPanel.add(nicknameLabel);
        clientInformationPanel.add(nicknameField);
        clientInformationPanel.add(emailAddressLabel);
        clientInformationPanel.add(emailAddressField);

        return clientInformationPanel;
    }

    private JPanel createServerInformationPanel(int textFieldWidth) {

        GridBagLayout gridBag = new GridBagLayout();

        JPanel serverInformationPanel = new JPanel();
        TitledBorder titledBorder = new TitledBorder(new EtchedBorder(), "Server Information:");
        serverInformationPanel.setBorder(titledBorder);
        serverInformationPanel.setLayout(gridBag);

        iPField = new JTextField(textFieldWidth);
        portField = new JTextField(textFieldWidth);

        JLabel iPLabel = new JLabel("Server IP:");
        iPLabel.setDisplayedMnemonic('I');
        iPLabel.setLabelFor(iPField);

        JLabel portLabel = new JLabel("Server port:");
        portLabel.setDisplayedMnemonic('p');
        portLabel.setLabelFor(portField);

        gridBag.setConstraints(iPLabel, createConstraints(0, 0));
        gridBag.setConstraints(iPField, createConstraints(1, 0));
        gridBag.setConstraints(portLabel, createConstraints(0, 1));
        gridBag.setConstraints(portField, createConstraints(1, 1));

        serverInformationPanel.add(iPLabel);
        serverInformationPanel.add(iPField);
        serverInformationPanel.add(portLabel);
        serverInformationPanel.add(portField);

        return serverInformationPanel;
    }

    private GridBagConstraints createConstraints(int x, int y) {

        GridBagConstraints gridBagConstraints = new GridBagConstraints();

        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;

        gridBagConstraints.gridx = x;
        gridBagConstraints.gridy = y;

        gridBagConstraints.insets = x == 0 ? new Insets(10, 5, 5, 5) : new Insets(5, 5, 5, 5);

        gridBagConstraints.anchor = x == 0 ? GridBagConstraints.LINE_START : GridBagConstraints.LINE_END;

        return gridBagConstraints;
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
                new ConnectionDialog_1_0_1(0);
                new ConnectionDialog_1_0_1(400);
                new ConnectionDialog_1_0_1(800);

            }
        });
    }
}
