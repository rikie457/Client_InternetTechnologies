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
    private String username;
    private String host;
    private int port;
    private Thread messageHandler;

    private JPanel panel;
    private JTextArea text;
    private JScrollPane scroll;
    private JTextField input;
    private JButton send, clientlist, addgroup;
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
        if (level == 2) {
            panel.add(clientlist);

            clientlist.addActionListener(actionEvent -> {
                util.sendMessage("CLIENTLIST");
            });
            return;
        }


        panel = new JPanel();
        text = new JTextArea(20, 20);
        scroll = new JScrollPane(text);
        input = new JTextField(10);
        send = new JButton("Send");
        clientlist = new JButton("Clientlist");
        addgroup = new JButton("Create Group");


        panel.setLayout(new FlowLayout());
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        text.setEditable(false);

        panel.add(scroll);
        panel.add(input);
        panel.add(send);
        panel.add(addgroup);


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

        addgroup.addActionListener(actionEvent -> {
            String groupname = JOptionPane.showInputDialog(this, "Group name:");
            util.sendMessage("GROUPCREATE " + username + " " + groupname);
        });

        frame.add(panel);

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

