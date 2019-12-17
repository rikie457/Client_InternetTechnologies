package nl.MenTych;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.PrintWriter;
import java.util.ArrayList;

public class DirectMessageClient extends JFrame implements Runnable {

    private PrintWriter writer;
    private Client sender;
    private String reciever;
    private Thread messageHandler;

    private JTextArea text = new JTextArea(20, 20);
    private JScrollPane scroll;

    JTextField input;
    JButton send;
    private Util util;

    public DirectMessageClient(String title, Client ct, String reciever, PrintWriter writer) {
        this.setTitle(title);
        this.sender = ct;
        this.sender.openDirectMessages.add(this);
        this.reciever = reciever;
        this.writer = writer;
        this.util = new Util(writer);
    }

    @Override
    public void run() {
        DirectMessageClient client = this;
        JFrame frame = this;
        frame.setLocationRelativeTo(null);

        frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                messageHandler.stop();
                client.sender.openDirectMessages.remove(client);
                frame.dispose();
            }
        });

        createUI(frame);

    }

    private void createUI(JFrame frame) {
        JPanel panel = new JPanel();
        panel.setLayout(new FlowLayout());

        frame.setContentPane(panel);

        frame.setTitle(this.reciever);
        frame.setSize(300, 450);
        frame.setResizable(false);
        frame.setVisible(true);
    }
}
