package nl.MenTych;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class ConnectionHandler {

    private String host;
    private int port;
    private Socket socket;
    private InputStream input;
    private OutputStream output;
    private DataInputStream reader;
    private DataOutputStream writer;

    public ConnectionHandler(String host, int port) {
        this.host = host;
        this.port = port;

        try {
            this.socket = new Socket(host, port);
            this.input = socket.getInputStream();
            this.output = socket.getOutputStream();
            this.reader = new DataInputStream(this.input);
            this.writer = new DataOutputStream(this.output);


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

    public DataOutputStream getWriter() {
        return writer;
    }

    public DataInputStream getReader() {
        return reader;
    }

}
