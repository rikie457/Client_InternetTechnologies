package nl.MenTych;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.PrintWriter;

public class Client extends JFrame implements Runnable {

    private ConnectionHandler connection;
    private BufferedReader reader;
    private PrintWriter writer;
    String username, currentgroup;
    private String host;
    private int port;
    private Thread messageHandler;

    private JPanel panel;
    private JTextArea text = new JTextArea(20, 20);
    private JScrollPane scroll;
    private JTextField input;
    private JButton send, clientlistButton, addgroupButton, joingroupButton, removegroupButton;
    private Util util;

    public Client(String host, int port, String username) {
        this.host = host;
        this.port = port;
        this.username = username;
    }


    @Override
    public void run() {
        //Setup the frame and the panel inside.

        JFrame frame = this;
        createUI(frame, 1);

        this.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                writer.println("QUIT");
                writer.flush();
                messageHandler.stop();
                frame.dispose();
            }
        });

        try {
            connection = new ConnectionHandler(host, port);
            reader = connection.getReader();
            writer = connection.getWriter();

            this.util = new Util(writer);

            util.sendMessage("HELO " + username);

            // starting messageHandler in new Thread.
            messageHandler = new Thread(new MessageHandler(connection, text, this));
            messageHandler.start();

            util.sendMessage("VERSION");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void createUI(JFrame frame, int level) {


        panel = new JPanel();
        scroll = new JScrollPane(text);
        input = new JTextField(10);
        send = new JButton("Send");
        clientlistButton = new JButton("Clientlist");
        addgroupButton = new JButton("Create Group");
        joingroupButton = new JButton("Join Group");
        removegroupButton = new JButton("Delete Current Group");


        panel.setLayout(new FlowLayout());
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        text.setEditable(false);

        panel.add(scroll);
        panel.add(input);
        panel.add(send);


        if (level == 2) {
            panel.add(clientlistButton);
            panel.add(addgroupButton);
            panel.add(joingroupButton);
//            panel.add(DirectMessageButton);
            panel.add(removegroupButton);

            clientlistButton.addActionListener(actionEvent -> {
                writer.println("CLIENTLIST");
                writer.flush();
            });

//            DirectMessageButton.addActionListener(actionEvent -> {
//                System.out.println("Opening DirectMessageWindow");
//                writer.println("CLIENTLIST-DM");
//                writer.flush();
//            });

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

            removegroupButton.addActionListener(actionEvent -> {
                util.sendMessage("GROUPREMOVE " + currentgroup + " " + username);
            });
        }


        send.addActionListener(actionEvent -> {
            String message = input.getText();

            if (message.equals("Disconnect")) {
                util.sendMessage("QUIT");
                messageHandler.stop();

                // connection to server is lost or user is disconnected.
                text.append("Disconnected from the server\n");
            }
            util.sendMessage("BCST " + message);


            //Scroll down and clear the input
            JScrollBar vertical = scroll.getVerticalScrollBar();
            vertical.setValue(vertical.getMaximum());
            input.setText("");

        });

        frame.setContentPane(panel);
        frame.setTitle(username);
        frame.setSize(300, 600);
        frame.setResizable(false);
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setVisible(true);
    }

    void stop() {
        Thread.currentThread().stop();
    }
}

