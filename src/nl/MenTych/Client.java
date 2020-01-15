package nl.MenTych;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.DataOutputStream;
import java.util.ArrayList;

public class Client extends JFrame implements Runnable {

    public ArrayList<DirectMessageClient> openDirectMessages = new ArrayList<>();
    private ConnectionHandler connection;
    private DataOutputStream writer;
    String username, currentgroup = "";
    private String host;
    private int port;
    private Thread messageHandler;
    private JPanel panel;
    private JTextArea text = new JTextArea(20, 20);
    private JScrollPane scroll;
    private JButton DirectMessageButton;

    ArrayList<String> clientList = new ArrayList<>();
    ArrayList<String> clientListGroup = new ArrayList<>();

    JTextField input;
    JButton send, clientlistButton, kickFromGroupButton, leavegroupButton, addgroupButton, joingroupButton, removegroupButton;
    private Util util;

    public Client(String host, int port, String username) {
        this.host = host;
        this.port = port;
        this.username = username;
    }


    @Override
    public void run() {
        //Setup the frame and the panel inside.

        Client frame = this;
        frame.setLocationRelativeTo(null);

        createUI(frame, 1, false);

        this.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                util.sendMessage("QUIT");
                messageHandler.stop();
                for (DirectMessageClient c : openDirectMessages) {
                    c.dispose();
                }
                frame.dispose();
            }
        });

        connection = new ConnectionHandler(host, port);
        writer = connection.getWriter();

        this.util = new Util(writer, getUsername());

        util.sendMessage("HELO " + username);

        // starting messageHandler in new Thread.
        MessageHandler message = new MessageHandler(connection, text, this);
        messageHandler = new Thread(message);
        messageHandler.start();
    }

    void createUI(Client frame, int level, boolean groupOwner) {

        panel = new JPanel();
        scroll = new JScrollPane(text);
        input = new JTextField(10);
        send = new JButton("Send");
        clientlistButton = new JButton("Clientlist");
        DirectMessageButton = new JButton("Direct Message");
        clientlistButton = new JButton("Clientlist");
        addgroupButton = new JButton("Create Group");
        joingroupButton = new JButton("Join Group");
        removegroupButton = new JButton("Delete Current Group");
        leavegroupButton = new JButton("Leave Current Group");
        kickFromGroupButton = new JButton("Kick user from Current Group");

        panel.setLayout(new FlowLayout());
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        text.setEditable(false);
        input.setEnabled(false);

        panel.add(scroll);
        panel.add(input);
        panel.add(send);

        if (level == 2) {
            panel.add(clientlistButton);
            panel.add(DirectMessageButton);

            clientlistButton.addActionListener(actionEvent -> {
                util.sendMessage("CLIENTLIST");
            });

            DirectMessageButton.addActionListener(actionEvent -> {
                util.sendMessage("CLIENTLIST-DM");
            });


            if (!groupOwner && frame.currentgroup.equals("Main")) {
                addgroupButton.addActionListener(actionEvent -> {
                    String groupname = JOptionPane.showInputDialog(this, "Group name:");
                    if (groupname != null) {
                        util.sendMessage("GROUPCREATE " + username + " " + groupname);
                    }
                });

                joingroupButton.addActionListener(actionEvent -> {
                    String groupname = JOptionPane.showInputDialog(this, "Group name:");
                    if (groupname != null) {
                        util.sendMessage("GROUPJOIN " + groupname);
                    }
                });

                panel.add(addgroupButton);
                panel.add(joingroupButton);
            } else if (!groupOwner) {
                leavegroupButton.addActionListener(actionEvent -> {
                    util.sendMessage("LEAVEGROUP");
                });
                panel.add(leavegroupButton);
            } else {
                kickFromGroupButton.addActionListener(actionEvent -> {
                    util.sendMessage("CLIENTLIST-GROUP");
                });

                removegroupButton.addActionListener(actionEvent -> {
                    util.sendMessage("GROUPREMOVE " + currentgroup + " " + username);
                });

                panel.add(kickFromGroupButton);
                panel.add(removegroupButton);
            }

            send.setEnabled(false);
            send.addActionListener(actionEvent -> {
                String message = input.getText();

                if (message.equals("Disconnect")) {
                    util.sendMessage("QUIT");
                    messageHandler.stop();

                    // connection to server is lost or user is disconnected.
                    text.append("Disconnected from the server\n");
                }

                if (message.length() > 0) {
                    util.sendMessage("BCST " + message);
                }


                //Scroll down and clear the input
                JScrollBar vertical = scroll.getVerticalScrollBar();
                vertical.setValue(vertical.getMaximum());
                input.setText("");

            });
        }

        frame.setContentPane(panel);
        frame.setTitle(username);
        frame.setSize(300, 600);
        frame.setResizable(false);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setVisible(true);
    }

    void stop() {
        Thread.currentThread().stop();
    }

    void openDirectMessageWindow(ArrayList<String> userlist, boolean groupKick) {
        if (userlist.size() > 1) {
            Thread DM;
            if (groupKick) {
                DM = new Thread(new ClientListWindow(this.username, userlist, this.writer, this, true, util));
            } else {
                DM = new Thread(new ClientListWindow(this.username, userlist, this.writer, this, util));
            }
            DM.start();
        } else {
            JOptionPane.showMessageDialog(this, "You are the only user currently connected.", "ERROR", JOptionPane.ERROR_MESSAGE);
        }
    }

    boolean isDirectMessageWindowOpen(String username) {
        if (openDirectMessages.size() > 0) {
            for (DirectMessageClient dm : openDirectMessages) {
                if (dm.getReciever().equals(username)) {
                    return true;
                }
            }
        }
        return false;
    }

    DirectMessageClient getDirectMessageClient(String username) {
        for (DirectMessageClient dm : openDirectMessages) {
            if (dm.getReciever().equals(username)) {
                return dm;
            }
        }
        return null;
    }

    void serverMessage(String message) {
        this.text.append(message + '\n');
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public String getUsername() {
        return username;
    }

    public ConnectionHandler getConnection() {
        return connection;
    }

}

