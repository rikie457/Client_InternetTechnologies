package nl.MenTych;

import java.io.BufferedReader;
import java.io.PrintWriter;
import java.util.Scanner;

public class Client {

    private ConnectionHandler connection;
    private BufferedReader reader;
    private PrintWriter writer;
    private String username;
    private Thread keeper;

    public Client(String host, int port, String username) {
        this.username = username;
        Scanner scanner = new Scanner(System.in);

        try {
            connection = new ConnectionHandler(host, port);
            reader = connection.getReader();
            writer = connection.getWriter();

            writer.println("HELO " + username);
            writer.flush();

            // user is connected to the server, printing username.
            while (!reader.readLine().contains("+OK HELO")) {
                System.out.println("Connected user " + username);
            }

            // starting messageHandler in new Thread.
            Thread messageHandler = new Thread(new MessageHandler(connection));
            messageHandler.start();

            // sending messages happens here.
            while (!scanner.nextLine().equals("Disconnect")) {

                String message = scanner.nextLine();
                writer.println("BCST " + message);
                writer.flush();
            }

            writer.println("QUIT");
            writer.flush();
            keeper.stop();

            // connection to server is lost or user is disconnected.
            while (!reader.readLine().equals("+OK Goodbye")) {
                System.out.println("Disconnected from the server");
            }


        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    }

