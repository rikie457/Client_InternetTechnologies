package nl.MenTych;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Arrays;

public class Util {

    private DataOutputStream writer;
    private Client sender;

    public Util(DataOutputStream writer, Client sender) {
        this.writer = writer;
        this.sender = sender;
    }

    public void sendMessage(String message) {
        try {
            System.out.println(sender.getUsername() + " SENDNG: " + message);
            writer.writeUTF(message);
            writer.flush();
        } catch (IOException e) {
            sender.openExceptionWindow(sender, e.getMessage());
            sender.stop();
        }
    }

    public void sendBytes(byte[] message) {
        try {
            System.out.println(sender.getUsername() + " SENDNG BYTES: " + Arrays.toString(message));
            writer.write(message);
            writer.flush();
        } catch (IOException e) {
            sender.openExceptionWindow(sender, e.getMessage());
            sender.stop();
        }
    }

}
