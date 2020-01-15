package nl.MenTych;

import javax.swing.*;
import java.io.*;

public class FileSendHandler implements Runnable {

    private String host;
    private int port;
    private ConnectionHandler connection, mainConnection;
    private Util mainutil, thisutil;


    public FileSendHandler(String host, int port, ConnectionHandler connection) {
        this.host = host;
        this.port = port;
        this.mainConnection = connection;
    }

    @Override
    public void run() {
        connection = new ConnectionHandler(host, port);
        try {
            boolean ready = false;

            while (!ready) {
                if (connection.getReader().readUTF().equals("FILERECIEVEREADY")) {
                    ready = true;
                }
            }

            mainutil = new Util(mainConnection.getWriter());
            thisutil = new Util(connection.getWriter());

            JFileChooser chooser = new JFileChooser();
            int returnVal = chooser.showOpenDialog(null);
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                sendFile(chooser.getSelectedFile());
            } else {
                mainutil.sendMessage("DONEFILE");
                mainutil.sendMessage("CANCELFILE");
                this.kill();
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("SOMETHING WHEN WRONG WHILE INITIATING CONNECTION STOPPING");
            kill();
        }
    }

    private void sendFile(File file) {
        try {
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
            mainutil.sendMessage("DONEFILE");
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("SOMETHING WHEN WRONG  WHILE SENDING FILE STOPPING");
            kill();
        }
    }

    void kill() {
        System.out.println("STOPPING");
        Thread.currentThread().stop();
    }


}
