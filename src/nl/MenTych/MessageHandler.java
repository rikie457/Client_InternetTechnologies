package nl.MenTych;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;


public class MessageHandler implements Runnable {
    private PrintWriter writer;
    private BufferedReader reader;

    /**
     * @param connection
     *
     * The message handler handles incomming messages from the server, including the heartbeat.
     *
     */
    public MessageHandler(ConnectionHandler connection) {
        this.writer = connection.getWriter();
        this.reader = connection.getReader();
    }

    @Override
    public void run() {
        System.out.println("recieving messages...");

        while (true) {
            try {
                String line = this.reader.readLine();

                // triggers when the recieved message hasn't been send by this client
                if (!line.contains("+OK BCST")) {

                    // triggers when a message is send to all clients.
                    if (line.contains("BCST")) {
                        messageRecieved(line);
                    }

                    // triggers when the server asks for a heartbeat.
                    // THE CLIENT NEEDS TO RESPOND WITH 'PONG' WITHIN 3 SECONDS.
                    if (line.equals("PING")) {
                        sendHeartbeat();
                    }
                } else {
                    // the message send by this client had been recieved properly bij the server
                    messageSendSuccessfully();
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void messageSendSuccessfully() {
        Util.printWithColor(Util.Color.MAGENTA, "Message send.");
    }

    private void messageRecieved(String message) {
        Util.printWithColor(Util.Color.GREEN, "Message recieved.");
        System.out.println(message);
    }

    private void sendHeartbeat() {
        Util.printWithColor(Util.Color.RED, "Found one!");

        // responding to the server.
        writer.println("PONG");
        writer.flush();
    }
}
