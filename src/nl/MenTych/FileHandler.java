package nl.MenTych;

import javax.swing.*;
import java.awt.*;
import java.io.*;

public class FileHandler implements Runnable {

    private String host;
    private int port;
    private ConnectionHandler connection, mainConnection;
    private Util mainutil;

    public FileHandler(String host, int port, ConnectionHandler connection) {
        this.host = host;
        this.port = port;
        this.mainConnection = connection;
    }

    @Override
    public void run() {
        connection = new ConnectionHandler(host, port);
        try {
            boolean ready = false;
            int type = 0; //NO TYPE YET
            while (!ready) {
                if (connection.getReader().readUTF().equals("FILERECIEVEREADY")) {
                    ready = true;
                    type = 1; //SEND FILE TO SERVER
                } else if (connection.getReader().readUTF().equals("FILESENDREADY")) {
                    ready = true;
                    type = 2; //RECIEVE FILE FROM SERVER
                }
            }

            if (type == 1) {
                JFileChooser chooser = new JFileChooser();
                int returnVal = chooser.showOpenDialog(null);
                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    System.out.println("You chose to open this file: " + chooser.getSelectedFile().getName());
                    sendFile(chooser.getSelectedFile());
                } else {
                    mainutil = new Util(mainConnection.getWriter());
                    mainutil.sendMessage("CANCELFILE");
                    this.kill();
                }
            } else if (type == 2) {
                recieveFile();
            }
        } catch (Exception e) {
            System.out.println("SOMETHING WHEN WRONG WHILE INITIATING CONNECTION STOPPING");
            kill();
        }
    }

    private void sendFile(File file) {
        try {
            System.out.println(file.length());
            byte[] mybytearray = new byte[(int) file.length()];

            FileInputStream fis = new FileInputStream(file);

            DataInputStream dis = new DataInputStream(fis);
            dis.readFully(mybytearray, 0, mybytearray.length);

            OutputStream os = connection.getOutput();

            os.flush();

            //Sending file name and file size to the server
            DataOutputStream dos = new DataOutputStream(os);
            dos.writeUTF(file.getName());
            System.out.println(file.getName());
            dos.writeLong(mybytearray.length);
            System.out.println(mybytearray.length);
            dos.write(mybytearray, 0, mybytearray.length);
            dos.flush();
            mainutil.sendMessage("SUCCESFILE");
            this.kill();
        } catch (Exception e) {
            System.out.println("SOMETHING WHEN WRONG  WHILE SENDING FILE STOPPING");
            kill();
        }
    }

    private void recieveFile() {
        try {
            int bytesRead;
            InputStream in = null;
            in = connection.getReader();

            DataInputStream clientData = new DataInputStream(in);

            String fileName = clientData.readUTF();
            System.out.println(fileName);
            FileOutputStream output = new FileOutputStream("files/" + fileName);

            long size = clientData.readLong();
            System.out.println(size);
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
        } catch (Exception e) {
            System.out.println("SOMETHING WHEN WRONG  WHILE RECIEVING FILE STOPPING");
            kill();
        }
    }

    void kill() {
        Thread.currentThread().stop();
    }


}
