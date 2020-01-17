package nl.MenTych;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Arrays;

public class Util {

    private DataOutputStream writer;
    private String sender;

    public Util(DataOutputStream writer, String sender) {
        this.writer = writer;
        this.sender = sender;
    }

    public void sendMessage(String message) {
        try {
            System.out.println(sender + " SENDNG: " + message);
            writer.writeUTF(message);
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendBytes(byte[] message) {
        try {
            System.out.println(sender + " SENDNG BYTES: " + Arrays.toString(message));
            writer.write(message);
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
