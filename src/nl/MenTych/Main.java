package nl.MenTych;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class Main {



    public static void main(String[] args) {
      Client client = new Client("127.0.0.1", 1337);
    }
}
