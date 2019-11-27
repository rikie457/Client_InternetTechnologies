package nl.MenTych;

import java.io.BufferedReader;
import java.io.PrintWriter;
import java.util.Scanner;

public class Client {

    private ConnectionHandler connection;
    private BufferedReader reader;
    private PrintWriter writer;
    private String username;

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
                System.out.print("User ");
                Util.printWithColor(Util.Color.BLUE, username);
                System.out.println(" is connected.");
            }

            // starting messageHandler in new Thread.
            Thread messageHandler = new Thread(new MessageHandler(connection));
            messageHandler.start();

            // sending messages happens here.
            while (scanner.hasNext()) {
                String message = scanner.nextLine();

                if (message.equals("Disconnect")) {
                    break;
                }

                writer.println("BCST " + message);
                writer.flush();
            }

            writer.println("QUIT");
            writer.flush();
            messageHandler.stop();

            // connection to server is lost or user is disconnected.
            System.out.println("Disconnected from the server");

        } catch (Exception e) {
            e.printStackTrace();
        }

    }


}

