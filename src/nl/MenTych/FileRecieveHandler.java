package nl.MenTych;

import java.awt.*;
import java.io.*;

public class FileRecieveHandler implements Runnable {

    private String host, filename;
    private int port;
    private ConnectionHandler connection, mainConnection;
    private Util mainutil, thisutil;


    public FileRecieveHandler(String host, int port, ConnectionHandler connection, String filename) {
        this.host = host;
        this.port = port;
        this.mainConnection = connection;
        this.filename = filename;
    }

    @Override
    public void run() {

        connection = new ConnectionHandler(host, port);
        mainutil = new Util(mainConnection.getWriter());
        thisutil = new Util(connection.getWriter());
        mainutil.sendMessage("DOWNLOADFILE " + filename);
        try {
            boolean ready = false;
            while (!ready) {
                if (connection.getReader().readUTF().equals("FILESENDREADY")) {
                    ready = true;
                }
            }


            thisutil.sendMessage("FILESENDREADY");
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
            FileOutputStream output = new FileOutputStream("files/" + fileName);

            long size = clientData.readLong();
            byte[] buffer = new byte[1024];
            while (size > 0 && (bytesRead = clientData.read(buffer, 0, (int) Math.min(buffer.length, size))) != -1) {
                output.write(buffer, 0, bytesRead);
                size -= bytesRead;
            }

            output.close();

            if (!Desktop.isDesktopSupported()) {
                System.out.println("Desktop is not supported");
                return;
            }

            Desktop desktop = Desktop.getDesktop();
            File file = new File("files/" + fileName);
            if (file.exists()) desktop.open(file);
            mainutil.sendMessage("DONEFILE");
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("SOMETHING WHEN WRONG  WHILE RECIEVING FILE STOPPING");
            kill();
        }
    }

    void kill() {
        System.out.println("STOPPING");
        Thread.currentThread().stop();
    }


}
