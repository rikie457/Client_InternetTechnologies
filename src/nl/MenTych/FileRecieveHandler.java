package nl.MenTych;

import java.io.*;

public class FileRecieveHandler implements Runnable {

    private String host;
    private int port;
    private ConnectionHandler connection, mainConnection;
    private Util mainutil;
    private Client client;


    public FileRecieveHandler(String host, int port, ConnectionHandler connection, String filename, Client client) {
        this.host = host;
        this.port = port;
        this.mainConnection = connection;
        this.client = client;
    }

    @Override
    public void run() {

        try {
            connection = new ConnectionHandler(host, port);
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println(port);
        System.out.println(host);
        mainutil = new Util(mainConnection.getWriter(), client);
        try {
            boolean ready = false;
            while (!ready) {
                if (connection.getReader().readUTF().equals("FILESENDREADY")) {
                    ready = true;
                }
            }
            recieveFile();
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("SOMETHING WHEN WRONG WHILE INITIATING CONNECTION STOPPING");
            kill();
        }
    }

    private void recieveFile() {
        try {
            int bytesRead;
            InputStream in = null;
            in = connection.getInput();

            DataInputStream clientData = new DataInputStream(in);

            String fileName = clientData.readUTF();

            File file = new File("files");
            boolean bool = file.mkdir();

            FileOutputStream output = new FileOutputStream("files/" + fileName);

            long size = clientData.readLong();
            byte[] buffer = new byte[1024];
            while (size > 0 && (bytesRead = clientData.read(buffer, 0, (int) Math.min(buffer.length, size))) != -1) {
                output.write(buffer, 0, bytesRead);
                size -= bytesRead;
            }
            output.close();
            mainutil.sendMessage("RECIEVEDFILE " + fileName);
            kill();
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("SOMETHING WHEN WRONG  WHILE RECIEVING FILE STOPPING");
            kill();
        }
    }

    void kill() {
        try {
            connection.getSocket().close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("STOPPING FILERECIEVEHANDLER " + client.getUsername());
        Thread.currentThread().stop();
    }


}
