package nl.MenTych;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;

public class ConnectionKeeper implements Runnable {
    private PrintWriter writer;
    private BufferedReader reader;

    public ConnectionKeeper(ConnectionHandler connection) {
        this.writer = connection.getWriter();
        this.reader = connection.getReader();
    }

    @Override
    public void run() {
        System.out.println("Looking for pings.....");
        while (true) {
            try {
                if (this.reader.readLine().equals("PING")) {
                    System.out.println("Found one!");
                    writer.println("PONG");
                    writer.flush();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
