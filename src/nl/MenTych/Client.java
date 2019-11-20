package nl.MenTych;

import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class Client {

    private Socket socket = null;
    private InputStream input = null;
    private OutputStream out = null;

    public Client(String host, int port) {
        Scanner scanner = new Scanner(System.in);
        // Maak verbinding met de server.// Alsje lokaal wilt verbinden het SERVER_ADDRESS “127.0.0.1”.
        try {
            socket = new Socket(host, port);
            System.out.println("Connected");


            // takes input from terminal
            input = socket.getInputStream();

            // sends output to the socket
            out = socket.getOutputStream();
        } catch (Exception e) {
            e.printStackTrace();
        }
        BufferedReader reader = new BufferedReader(new InputStreamReader(input));
        PrintWriter writer = new PrintWriter(out);
        try {
            String line = reader.readLine();
            while (!scanner.nextLine().equals("Over")) {
                writer.println("BCST " + scanner.nextLine());
                writer.flush();
            }
            } catch(IOException i){
                System.out.println(i);
            }
        }


    }

