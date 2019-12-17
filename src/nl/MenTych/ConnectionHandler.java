package nl.MenTych;

import java.io.*;
import java.net.Socket;

public class ConnectionHandler {

    private String host;
    private int port;
    private Socket socket;
    private InputStream input;
    private OutputStream output;
    private BufferedReader reader;
    private PrintWriter writer;

    public ConnectionHandler(String host, int port) {
        this.host = host;
        this.port = port;

        try {
            this.socket = new Socket(host, port);
            this.input = socket.getInputStream();
            this.output = socket.getOutputStream();
            this.reader = new BufferedReader(new InputStreamReader(this.input));
            this.writer = new PrintWriter(this.output, true);



        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    Socket getSocket() {
        return socket;
    }

    public InputStream getInput() {
        return input;
    }

    public OutputStream getOutput() {
        return output;
    }

    public PrintWriter getWriter() {
        return writer;
    }

    public BufferedReader getReader() {
        return reader;
    }
}
