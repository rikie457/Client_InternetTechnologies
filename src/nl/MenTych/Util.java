package nl.MenTych;

import java.io.DataOutputStream;
import java.io.IOException;

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
}
