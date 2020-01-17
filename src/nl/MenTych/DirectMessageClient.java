package nl.MenTych;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.DataOutputStream;
import java.security.PublicKey;

public class DirectMessageClient extends JFrame implements Runnable {

    private final Encryption encryption;
    private Client sender;
    private String reciever;
    private JTextArea text = new JTextArea(20, 20);
    private JScrollPane scroll;
    private JTextField input;
    private Util util;

    public PublicKey recieversPublicKey;

    public DirectMessageClient(String title, Client ct, String reciever, DataOutputStream writer) {
        this.setTitle("Direct Message to:" + title);
        this.sender = ct;
        this.sender.openDirectMessages.add(this);
        this.reciever = reciever;
        this.util = new Util(writer, ct);

        new Guard(this.sender.getUsername(), 2048);
        this.encryption = new Encryption(this.sender.getUsername());
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
        JButton send = new JButton("Send");
        JButton sendFile = new JButton("Send a File");

        text.setEditable(false);

        panel.setLayout(new FlowLayout());
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

        panel.add(scroll);
        panel.add(input);
        panel.add(send);
        panel.add(sendFile);

        send.addActionListener(actionEvent -> {
            String message = input.getText();

            if (message.length() > 0) {
                String encryptedtext = encryption.encryptText(message, recieversPublicKey);

                util.sendMessage("DM " + reciever + " " + this.sender.getUsername() + " " + encryptedtext);
                appendToTextView("You send an encrypted message: " + message);
            }

            //Scroll down and clear the input
            JScrollBar vertical = scroll.getVerticalScrollBar();
            vertical.setValue(vertical.getMaximum());
            input.setText("");
        });

        sendFile.addActionListener(actionEvent -> {
            FileSendHandler fsh = new FileSendHandler(sender.getHost(), sender.getPort() + 1, sender.getConnection(), reciever, sender);

            Thread fileHandler = new Thread(fsh);
            fileHandler.start();
        });

        frame.setContentPane(panel);

        frame.setSize(300, 450);
        frame.setResizable(false);
        frame.setVisible(true);

        PublicKey pubKey = this.encryption.getPublic();
        byte[] keyBytes = pubKey.getEncoded();

        util.sendMessage("+KEY PUBLIC " + reciever + " " + this.sender.getUsername());
        util.sendBytes(keyBytes);
    }

    public String getReciever() {
        return reciever;
    }

    public void appendToTextView(String text) {
        this.text.append(text);
        this.text.append("\n");
    }
}
