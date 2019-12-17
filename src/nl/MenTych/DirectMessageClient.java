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
        this.setTitle("Direct Message to:" + title);
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
                client.sender.openDirectMessages.remove(client);
                frame.dispose();
            }
        });

        createUI(frame);

    }

    private void createUI(JFrame frame) {
        JPanel panel = new JPanel();
        panel.setLayout(new FlowLayout());

        scroll = new JScrollPane(text);
        input = new JTextField(10);
        send = new JButton("Send");

        text.setEditable(false);

        panel.setLayout(new FlowLayout());
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

        panel.add(scroll);
        panel.add(input);
        panel.add(send);

        send.addActionListener(actionEvent -> {
            String message = input.getText();

            if (message.length() > 0) {
                util.sendMessage("DM " + reciever + " " + this.sender.username + " " + message);
                appendToTextView("You: " + message);
            }


            //Scroll down and clear the input
            JScrollBar vertical = scroll.getVerticalScrollBar();
            vertical.setValue(vertical.getMaximum());
            input.setText("");

        });

        frame.setContentPane(panel);

        frame.setSize(300, 450);
        frame.setResizable(false);
        frame.setVisible(true);
    }

    public String getReciever() {
        return reciever;
    }

    public void appendToTextView(String text) {
        this.text.append(text);
        this.text.append("\n");
    }
}
