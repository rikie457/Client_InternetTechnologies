package nl.MenTych;

import javax.swing.*;
import java.io.BufferedReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;


public class MessageHandler implements Runnable {
    private PrintWriter writer;
    private BufferedReader reader;
    private JTextArea text;
    private Client ct;
    private Util util;

    /**
     * @param connection The message handler handles incomming messages from the server, including the heartbeat.
     */
    public MessageHandler(ConnectionHandler connection, JTextArea text, Client ct) {
        this.writer = connection.getWriter();
        this.reader = connection.getReader();
        this.ct = ct;
        this.text = text;
        this.util = new Util(writer);
    }

    @Override
    public void run() {

        try {
            String line = this.reader.readLine();
            while (!line.contains("+OK HELO")) {
                line = this.reader.readLine();
                if (line.equals("-ERR user already logged in")) {
                    JOptionPane.showMessageDialog(ct, "Username already taken", "ERROR", JOptionPane.ERROR_MESSAGE);
                    ct.dispose();
                    ct.stop();
                    this.kill();
                }
            }

            System.out.println("Client is ready to send and recieve messages!\n");
            ct.currentgroup = "Main";

            util.sendMessage("VERSION");

            ct.send.setEnabled(true);
            ct.input.setEnabled(true);

            while (true) {
                line = this.reader.readLine();
                System.out.println(line);

                String[] splits = line.split("\\s+");

                if (splits.length >= 2 && !splits[0].equals("BCST")) {

                    System.out.println(splits[0] + " " + splits[1]);
                    switch (splits[0] + " " + splits[1]) {

                        case "-ERR NOSUCHGROUP":
                            JOptionPane.showMessageDialog(ct, "Group does not exist", "ERROR", JOptionPane.ERROR_MESSAGE);
                            break;
                        case "-ERR GROUPEXISTS":
                            JOptionPane.showMessageDialog(ct, "Group already exists", "ERROR", JOptionPane.ERROR_MESSAGE);
                            break;
                        case "-ERR NOTOWNER":
                            JOptionPane.showMessageDialog(ct, "You are not the owner\n Only owners can remove groups", "ERROR", JOptionPane.ERROR_MESSAGE);
                            break;
                        case "+OK GROUPJOIN":
                            ct.currentgroup = splits[2];
                            text.append("Joined group " + splits[2] + "\n");
                            break;
                        case "+OK GROUPCREATE":
                            ct.currentgroup = splits[2];
                            text.append("Joined group " + splits[2] + "\n");
                            break;
                        case "+OK GROUPREMOVED":
                            ct.currentgroup = "Main";
                            util.sendMessage("GROUPJOIN  Main");
                            text.append("The group you joined has been removed \n Moving back to Main \n");
                            break;
                        case "+OK CLIENTLIST":
                            String[] members = line.replaceAll("[*+OK CLIENTLIST $]", "").split(",");
                            ct.clientList.clear();

                            messageRecieved("Clientlist" + ":");
                            for (String member : members) {
                                ct.clientList.add(member);
                                messageRecieved(" - " + member);
                            }
                            break;

                        case "+OK CLIENTLIST-DM":
                            String[] users = line.replaceAll("[*+OK CLIENTLIST\\-DM $]", "").split(",");
                            ct.clientList.clear();

                            for (String member : users) {
                                ct.clientList.add(member);
                            }

                            ct.openDirectMessageWindow();
                            break;
                        case "+OK BCST":
                            // the message send by this client had been recieved properly by the server
                            // also split up the message and sanitize the message.
                            String[] parts = line.split("\\+OK BCST");
                            String message = parts[1];
                            messageSendSuccessfully("You: " + message);
                            break;

                        case "+VERSION 2":
                            this.ct.createUI(this.ct, 2);
                            ct.send.setEnabled(true);
                            ct.input.setEnabled(true);

                            System.out.println("VERSION 2");
                            break;

                        case "DSCN Pong":
                            ct.stop();
                            System.out.println("STOPPING CLIENT");
                            kill();
                            break;
                    }
                } else {
                    if (splits[0].equals("PING")) {
                        sendHeartbeat();
                    } else if (splits[0].equals("BCST")) {
                        // triggers when a message is send to all clients
                        //Split up the message and sanitize the message.
                        String name = line.substring(line.indexOf("[") + 1, line.indexOf("]"));
                        String[] parts = line.split("BCST \\[+\\w+\\] ");
                        String message = parts[1];
                        messageRecieved(name + ": " + message);
                    }
                }

            }

        } catch (Exception e) {
            ct.stop();
            kill();
        }

    }

    private void messageSendSuccessfully(String message) {
        text.append(message + "\n");
    }

    private void messageRecieved(String message) {
        text.append(message + "\n");
    }

    private void sendHeartbeat() {
        // responding to the server.
        writer.println("PONG");
        writer.flush();
    }

    void kill() {
        Thread.currentThread().stop();
    }
}
