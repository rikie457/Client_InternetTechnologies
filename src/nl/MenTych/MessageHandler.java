package nl.MenTych;

import javax.swing.*;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;


public class MessageHandler implements Runnable {
    private DataOutputStream writer;
    private DataInputStream reader;
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
        this.util = new Util(writer, ct.getUsername());
    }

    @Override
    public void run() {
        try {
            String line = this.reader.readUTF();
            while (!line.contains("+OK HELO")) {
                line = this.reader.readUTF();
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
                line = this.reader.readUTF();
                System.out.println(this.ct.username + " RECIEVING: " + line);
                    String[] splits = line.split("\\s+");

                    if (splits.length >= 2 && !splits[0].equals("BCST") && !splits[0].equals("+DM")) {
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
                                ct.createUI(this.ct, 2, false);
                                break;
                            case "+OK GROUPCREATE":
                                ct.currentgroup = splits[2];
                                text.append("Created group " + splits[2] + " (You are now the owner)\n");
                                this.ct.createUI(this.ct, 2, true);
                                ct.send.setEnabled(true);
                                ct.input.setEnabled(true);
                                break;
                            case "+OK GROUPREMOVED":
                                ct.currentgroup = "Main";
                                util.sendMessage("GROUPJOIN  Main");
                                text.append("The group you joined has been removed \n Moving back to Main \n");
                                break;

                            case "+OK GROUPKICK":
                                ct.currentgroup = "Main";
                                util.sendMessage("GROUPJOIN  Main");
                                text.append("You have been kicked from the group by it's owner. \n Moving back to Main \n");
                                break;

                            case "+OK GROUPLEAVE":
                                ct.currentgroup = "Main";
                                util.sendMessage("GROUPJOIN  Main");
                                text.append("You have leaved the group. \n Moving back to Main \n");
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
                                String[] users = line.replaceAll("\\WOK \\bCLIENTLIST-DM[\\s]", "").split(",");
                                ct.clientList.clear();
                                for (String member : users) {
                                    ct.clientList.add(member);
                                }
                                ct.openDirectMessageWindow(ct.clientList, false);
                                break;

                            case "+OK CLIENTLIST-GROUP":
                                String[] groupmembers = line.replaceAll("\\WOK \\bCLIENTLIST-GROUP[\\s]", "").split(",");
                                ct.clientListGroup.clear();
                                for (String member : groupmembers) {
                                    ct.clientListGroup.add(member);
                                }
                                ct.openDirectMessageWindow(ct.clientListGroup, true);
                                break;

                            case "+OK BCST":
                                // the message send by this client had been recieved properly by the server
                                // also split up the message and sanitize the message.
                                String[] parts = line.split("\\+OK BCST");
                                String message = parts[1];
                                messageSendSuccessfully("You: " + message);
                                break;

                            case "+VERSION 2":
                                this.ct.createUI(this.ct, 2, false);
                                ct.send.setEnabled(true);
                                ct.input.setEnabled(true);

                                System.out.println("VERSION 2");
                                break;

                            case "+OK RECIEVEFILE":
                                FileRecieveHandler fileRecieveHandler = new FileRecieveHandler(ct.getHost(), ct.getPort() + 1, ct.getConnection(), splits[2], ct);
                                Thread filereciever = new Thread(fileRecieveHandler);
                                filereciever.start();

                                break;

                            case "DSCN Pong":
                                ct.stop();
                                System.out.println("STOPPING CLIENT");
                                kill();
                                break;
                        }
                    } else {
                        switch (splits[0]) {
                            case "PING":
                                sendHeartbeat();
                                break;

                            case "BCST":
                                // triggers when a message is send to all clients
                                //Split up the message and sanitize the message.
                                String name = line.substring(line.indexOf("[") + 1, line.indexOf("]"));
                                String[] parts = line.split("BCST \\[+\\w+\\] ");
                                String message = parts[1];
                                messageRecieved(name + ": " + message);
                                break;

                            case "+DM":
                                pushContentToTextView(splits, false);
                                break;
                        }
                    }

            }

        } catch (Exception e) {
            e.printStackTrace();
            ct.stop();
            kill();
        }
    }

    private void pushContentToTextView(String[] splits, boolean threadStarted) {
        if (this.ct.isDirectMessageWindowOpen(splits[1])) {
            StringBuilder txt = new StringBuilder();
            txt.append(splits[1]);
            txt.append(": ");

            for (int i = 2; i < splits.length; i++) {
                txt.append(splits[i]);
                txt.append(" ");
            }
            this.ct.getDirectMessageClient(splits[1]).appendToTextView(txt.toString());

        } else if (!threadStarted) {
            Thread t = new Thread(new DirectMessageClient(splits[1], this.ct, splits[1], writer));
            t.start();
            pushContentToTextView(splits, true);
        } else {
            pushContentToTextView(splits, true);
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
        util.sendMessage("PONG");
    }

    void kill() {
        try {
            this.ct.getConnection().getSocket().close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Thread.currentThread().stop();
    }
}
