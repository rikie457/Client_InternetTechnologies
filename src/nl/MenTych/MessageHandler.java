package nl.MenTych;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.security.MessageDigest;
import java.security.PublicKey;


public class MessageHandler implements Runnable {
    private DataOutputStream writer;
    private DataInputStream reader;
    private JTextArea text;
    private Client ct;
    private Util util;

    private final Encryption encryption;

    /**
     * @param connection The message handler handles incomming messages from the server, including the heartbeat.
     */
    public MessageHandler(ConnectionHandler connection, JTextArea text, Client ct) {
        this.writer = connection.getWriter();
        this.reader = connection.getReader();
        this.ct = ct;
        this.text = text;
        this.util = new Util(writer, ct);

        this.encryption = new Encryption(ct.getUsername());
    }

    private static String getFileChecksum(MessageDigest digest, File file) throws IOException {
        //Get file input stream for reading the file content
        FileInputStream fis = new FileInputStream(file);

        //Create byte array to read data in chunks
        byte[] byteArray = new byte[1024];
        int bytesCount = 0;

        //Read file data and update in message digest
        while ((bytesCount = fis.read(byteArray)) != -1) {
            digest.update(byteArray, 0, bytesCount);
        }

        //close the stream; We don't need it now.
        fis.close();

        //Get the hash's bytes
        byte[] bytes = digest.digest();

        //This bytes[] has bytes in decimal format;
        //Convert it to hexadecimal format
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < bytes.length; i++) {
            sb.append(Integer.toString((bytes[i] & 0xff) + 0x100, 16).substring(1));
        }

        //return complete hash
        return sb.toString();
    }

    private void pushContentToTextView(String[] splits, boolean threadStarted) {
        if (this.ct.isDirectMessageWindowOpen(splits[1])) {
            StringBuilder txt = new StringBuilder();
            txt.append(splits[1]);
            txt.append(": ");

            for (int i = 2; i < splits.length; i++) {
                txt.append(this.encryption.decryptText(splits[i]));
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
            ct.setCurrentgroup("Main");

            util.sendMessage("VERSION");

            ct.send.setEnabled(true);
            ct.input.setEnabled(true);

            while (true) {
                try {
                    line = this.reader.readUTF();
                    System.out.println(this.ct.getUsername() + " RECIEVING: " + line);
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
                                ct.setCurrentgroup(splits[2]);
                                text.append("Joined group " + splits[2] + "\n");
                                ct.createUI(this.ct, 2, false);
                                ct.send.setEnabled(true);
                                ct.input.setEnabled(true);
                                break;
                            case "+OK GROUPCREATE":
                                ct.setCurrentgroup(splits[2]);
                                text.append("Created group " + splits[2] + " (You are now the owner)\n");
                                this.ct.createUI(this.ct, 2, true);
                                ct.send.setEnabled(true);
                                ct.input.setEnabled(true);
                                break;
                            case "+OK GROUPREMOVED":
                                ct.setCurrentgroup("Main");
                                util.sendMessage("GROUPJOIN Main");
                                text.append("The group you joined has been removed \n Moving back to Main \n");
                                break;

                            case "+OK GROUPKICK":
                                ct.setCurrentgroup("Main");
                                util.sendMessage("GROUPJOIN  Main");
                                text.append("You have been kicked from the group by it's owner. \n Moving back to Main \n");
                                break;

                            case "+OK GROUPLEAVE":
                                ct.setCurrentgroup("Main");
                                util.sendMessage("GROUPJOIN Main");
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

                            case "+OK FILESERVEREADY":

                                break;

                            case "+OK CHECKSUM":
                                String filename = splits[2];
                                String serverChecksum = splits[3];
                                File file = new File("files/" + filename);
                                MessageDigest md5Digest = MessageDigest.getInstance("MD5");
                                String checksum = getFileChecksum(md5Digest, file);
                                if (serverChecksum.equals(checksum)) {
                                    if (!Desktop.isDesktopSupported()) {
                                        System.out.println("Desktop is not supported");
                                        return;
                                    }

                                    Desktop desktop = Desktop.getDesktop();
                                    if (file.exists()) {
                                        try {
                                            desktop.open(file);
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                }
                                break;

                            case "DSCN Pong":
                                ct.stop();
                                System.out.println("STOPPING CLIENT");
                                kill();
                                break;

                            case "+KEY PUBLIC":
                                try {
                                    int xtr = 0;

                                    byte[] buffer = new byte[1024];

                                    while (this.reader.read(buffer) == -1) {
                                        xtr++;
                                    }

                                    PublicKey pubkey = Encryption.getPublicKeyReciever(buffer);
                                    DirectMessageClient dmc = this.ct.getDirectMessageClient(splits[2]);
                                    dmc.recieversPublicKey = pubkey;
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }

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

                            case "DSCN Pong":
                                ct.stop();
                                System.out.println("STOPPING CLIENT");
                                kill();
                                break;
                        }
                    }

                } catch (Exception e) {
                    break;
                }

            }
        } catch (Exception e) {
            kill();
        }
    }

    void kill() {
        System.out.println("DROPPING CONNECTION " + ct.getUsername());
        ct.stop();
        Thread.currentThread().stop();
    }
}
