package nl.MenTych;

import javax.swing.*;
import java.io.*;

public class FileSendHandler implements Runnable {

    private String host;
    private int port;
    private ConnectionHandler connection, mainConnection;
    private Util mainutil, thisutil;
    private String reciever;
    private Client client;


    public FileSendHandler(String host, int port, ConnectionHandler connection, String reciever, Client client) {
        this.host = host;
        this.port = port;
        this.mainConnection = connection;
        this.reciever = reciever;
        this.client = client;
    }

    @Override
    public void run() {
            JFileChooser chooser = new JFileChooser();
            int returnVal = chooser.showOpenDialog(null);
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                sendFile(chooser.getSelectedFile());
            } else {
                this.kill();
            }
        }

    private void sendFile(File file) {
        try {
            mainutil = new Util(mainConnection.getWriter(), client.getUsername());
            mainutil.sendMessage("UPLOADFILE " + file.getName() + " " + reciever);

            boolean readyserver = false;
            while (!readyserver) {
                if (mainConnection.getReader().readUTF().equals("+OK FILESERVEREADY")) {
                    readyserver = true;
                }
            }

            connection = new ConnectionHandler(host, port);
            System.out.println(port);
            System.out.println(host);
            thisutil = new Util(connection.getWriter(), "FILESENDER FOR " + client.getUsername());

            boolean ready = false;
            while (!ready) {
                if (connection.getReader().readUTF().equals("FILERECIEVEREADY")) {
                    ready = true;
                }
            }
            byte[] mybytearray = new byte[(int) file.length()];

            FileInputStream fis = new FileInputStream(file);

            DataInputStream dis = new DataInputStream(fis);
            dis.readFully(mybytearray, 0, mybytearray.length);

            OutputStream os = connection.getOutput();

            os.flush();

            //Sending file name and file size to the server
            DataOutputStream dos = new DataOutputStream(os);
            dos.writeUTF(file.getName());
            dos.writeLong(mybytearray.length);
            dos.write(mybytearray, 0, mybytearray.length);
            dos.flush();
            kill();
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("SOMETHING WHEN WRONG  WHILE SENDING FILE STOPPING");
            kill();
        }
        }

    void kill() {
        try {
            connection.getSocket().close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("STOPPING FILESENDHANDLER " + client.getUsername());
        Thread.currentThread().stop();
    }


}
