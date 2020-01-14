package nl.MenTych;

import java.io.DataOutputStream;
import java.io.IOException;

public class Util {

    private DataOutputStream writer;

    public Util(DataOutputStream writer) {
        this.writer = writer;
    }

    public void sendMessage(String message) {
        try {
            System.out.println("SENDNG: " + message);
            writer.writeUTF(message);
            writer.flush();
        } catch (IOException e) {
            e.getStackTrace();
        }
    }
}
