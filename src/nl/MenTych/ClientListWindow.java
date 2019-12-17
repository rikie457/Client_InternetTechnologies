package nl.MenTych;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.PrintWriter;
import java.util.ArrayList;

public class ClientListWindow extends JFrame implements Runnable {

    private final String username;
    private final ArrayList<String> clientList;
    private final PrintWriter writer;
    private final Client sender;
    private boolean groupKick = false;

    public ClientListWindow(String username, ArrayList<String> clientList, PrintWriter writer, Client sender) {
        this.username = username;
        this.clientList = clientList;
        this.writer = writer;
        this.sender = sender;
    }

    public ClientListWindow(String username, ArrayList<String> clientList, PrintWriter writer, Client sender, boolean groupkick) {
        this.username = username;
        this.clientList = clientList;
        this.writer = writer;
        this.sender = sender;
        this.setGroupKick();
    }

    public void setGroupKick() {
        this.groupKick = true;
    }

    @Override
    public void run() {
        ClientListWindow client = this;
        JFrame frame = this;

        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                frame.dispose();
            }
        });

        createUI(frame);
    }

    void createUI(JFrame frame) {
        JPanel panel = new JPanel();
        panel.setLayout(new FlowLayout());

        ArrayList<JButton> users = new ArrayList<>();

        for (String member : clientList) {
            if(!this.username.equals(member)) {
                JButton button = new JButton(member);

                if (!this.groupKick) {
                    button.addActionListener(actionEvent -> {
                        Thread t = new Thread(new DirectMessageClient(member, sender, member, writer));
                        t.start();
                        this.dispose();
                    });
                } else {
                    button.addActionListener(actionEvent -> {
                        writer.println("KICK " + member);
                        this.sender.serverMessage("You have kicked " + member);
                        this.dispose();
                    });
                }

                users.add(button);
            }
        }

        for (JButton user : users) {
            panel.add(user);
        }

        frame.setContentPane(panel);

        frame.setTitle("Direct Message");
        frame.setSize(300, 450);
        frame.setResizable(false);
        frame.setVisible(true);
    }
}
