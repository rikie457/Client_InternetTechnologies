package nl.MenTych;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import java.io.*;
import java.net.Socket;

public class ConnectionHandler {

    private String host;
    private int port;
    private Socket socket;
    private InputStream input;
    private OutputStream output;
    private DataInputStream reader;
    private DataOutputStream writer;

    public ConnectionHandler(String host, int port) throws Exception {
        this.host = host;
        this.port = port;

        System.out.println("Connecting to " + host + " on " + port);
        this.socket = new Socket(host, port);
        this.input = socket.getInputStream();
        this.output = socket.getOutputStream();
        this.reader = new DataInputStream(this.input);
        this.writer = new DataOutputStream(this.output);

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
