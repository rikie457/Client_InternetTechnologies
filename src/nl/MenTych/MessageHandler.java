package nl.MenTych;

import javax.swing.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;


public class MessageHandler implements Runnable {
    private PrintWriter writer;
    private BufferedReader reader;
    private JTextArea text;

    /**
     * @param connection The message handler handles incomming messages from the server, including the heartbeat.
     */
    public MessageHandler(ConnectionHandler connection, JTextArea text) {
        this.writer = connection.getWriter();
        this.reader = connection.getReader();
        this.text = text;
    }

    @Override
    public void run() {
        System.out.println("Client is ready to send and recieve messages!\n");

        while (true) {
            try {
                String line = this.reader.readLine();

                // triggers when the recieved message hasn't been send by this client
                if (!line.contains("+OK BCST")) {
                    // triggers when a message is send to all clients.
                    if (line.contains("BCST")) {
                        //Split up the message and sanitize the message.
                        String name = line.substring(line.indexOf("[") + 1, line.indexOf("]"));
                        String[] parts = line.split("BCST \\[+\\w+\\]");
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

            } catch (IOException e) {
                e.printStackTrace();
            }
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
        writer.println("PONG");
        writer.flush();
    }
}
