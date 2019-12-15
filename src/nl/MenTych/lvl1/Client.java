package nl.MenTych.lvl1;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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

    public Client(String host, int port, String username) {
        this.host = host;
        this.port = port;
        this.username = username;
    }


    @Override
    public void run() {
        //Setup the frame and the panel inside.
        JPanel panel = new JPanel();
        panel.setLayout(new FlowLayout());
        JTextArea text = new JTextArea(20, 20);
        JScrollPane scroll = new JScrollPane(text);
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        text.setEditable(false);
        JTextField input = new JTextField(10);
        JButton send = new JButton("Send");
        panel.add(scroll);
        panel.add(input);
        panel.add(send);
        this.add(panel);
        this.setTitle(username);
        this.setSize(300, 400);
        this.setResizable(false);
        this.setLocationRelativeTo(null);
        this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        this.setVisible(true);

        try {
            connection = new ConnectionHandler(host, port);
            reader = connection.getReader();
            writer = connection.getWriter();

            writer.println("HELO " + username);
            writer.flush();

            while (true) {
                System.out.println(reader.readLine());
                if (reader.readLine().equals("-ERR user already logged in")) {
                    stop();
                }

                if (reader.readLine().contains("+OK HELO")) {
                    break;
                }
            }


//            while(!reader.readLine().equals("-ERR user already logged in")){
//                System.out.println(reader.readLine());
//
//                this.dispose();
//                this.stop();
//            }
//                JOptionPane.showMessageDialog(this, "Username already in use. STOPPING", "ERROR", JOptionPane.ERROR_MESSAGE);


//            // user is connected to the server, printing username.
//            while (!reader.readLine().contains("+OK HELO")) {
//                text.append("User " + username + " is connected.\n");
////                Util.printWithColor(Util.Color.BLUE, username);
//            }

            // starting messageHandler in new Thread.
            messageHandler = new Thread(new MessageHandler(connection, text, this));
            messageHandler.start();

            send.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent actionEvent) {
                    String message = input.getText();
                    if (message.equals("Disconnect")) {
                        writer.println("QUIT");
                        writer.flush();
                        messageHandler.stop();

                        // connection to server is lost or user is disconnected.
                        text.append("Disconnected from the server\n");

                    }

                    writer.println("BCST " + message);
                    writer.flush();

                    //Scroll down and clear the input
                    JScrollBar vertical = scroll.getVerticalScrollBar();
                    vertical.setValue(vertical.getMaximum());
                    input.setText("");
                }
            });


        } catch (Exception e) {
            if (messageHandler != null) {
                messageHandler.stop();
            }
            this.dispose();
            stop();
        }

    }

    void stop() {
        this.dispose();
        Thread.currentThread().stop();

    }
}