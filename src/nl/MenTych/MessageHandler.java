package nl.MenTych;

import javax.swing.*;
import java.io.BufferedReader;
import java.io.PrintWriter;


public class MessageHandler implements Runnable {
    private PrintWriter writer;
    private BufferedReader reader;
    private JTextArea text;
    private Client ct;

    /**
     * @param connection The message handler handles incomming messages from the server, including the heartbeat.
     */
    public MessageHandler(ConnectionHandler connection, JTextArea text, Client ct) {
        this.writer = connection.getWriter();
        this.reader = connection.getReader();
        this.ct = ct;
        this.text = text;
    }

    @Override
    public void run() {

        try {
            String line = this.reader.readLine();
            while (!line.contains("+OK HELO")) {
                line = this.reader.readLine();
                if (line.equals("-ERR user already logged in") || line.equals("-ERR username has an invalid format (only characters, numbers and underscores are allowed")) {
                    JOptionPane.showMessageDialog(ct, "Username already taken", "ERROR", JOptionPane.ERROR_MESSAGE);
                    ct.dispose();
                    ct.stop();
                    this.kill();
                }
            }

            System.out.println("Client is ready to send and recieve messages!\n");

            while (true) {
                line = this.reader.readLine();
                String[] splits = line.split("\\s+");


                if (line.equals("DSCN Pong timeout")) {
                    ct.stop();
                    System.out.println("STOPPING CLIENT");
                    kill();
                }

                if (line.contains("+VERSION 2")) {
                    this.ct.createUI(this.ct, 2);
                    System.out.println("VERSION 2");
                }

                // triggers when the recieved message hasn't been send by this client
                if (line.contains("+OK CLIENTLIST")) {
                    String[] members = line.replaceAll("[*+OK CLIENTLIST $]", "").split(",");
                    messageRecieved("Clientlist" + ":");
                    for (String member : members) {
                        messageRecieved(" - " + member);
                    }


                } else if (!line.contains("+OK BCST")) {
                    // triggers when a message is send to all clients.
                    if (line.contains("BCST")) {
                        //Split up the message and sanitize the message.
                        String name = line.substring(line.indexOf("[") + 1, line.indexOf("]"));
                        String[] parts = line.split("BCST \\[+\\w+\\] ");
                        String message = parts[1];
                        messageRecieved(name + ": " + message);
                    }

                    // triggers when the server asks for a heartbeat.
                    // THE CLIENT NEEDS TO RESPOND WITH 'PONG' WITHIN 3 SECONDS.
                    if (line.equals("PING")) {
                        sendHeartbeat();
                    }


                } else {
                    // the message send by this client had been recieved properly by the server
                    // also split up the message and sanitize the message.
                    String[] parts = line.split("\\+OK BCST");
                    String message = parts[1];
                    messageSendSuccessfully("You: " + message);
                }

            }
        } catch (Exception e) {
            ct.stop();
            kill();
        }

    }

    private void messageSendSuccessfully(String message) {
        Util.printLnWithColor(Util.Color.MAGENTA, "Message send.");
        text.append(message + "\n");
    }

    private void messageRecieved(String message) {
        text.append(message + "\n");
    }

    private void sendHeartbeat() {
        // responding to the server.
        System.out.println("SENDING PONG");
        writer.println("PONG");
        writer.flush();
    }

    void kill() {
        Thread.currentThread().start();
    }
}
