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

            while (!reader.readLine().contains("+OK HELO")) {
                System.out.println("Connected user " + username);
            }

            Thread keeper = new Thread(new ConnectionKeeper(connection));
            keeper.start();

            while (!scanner.nextLine().equals("Disconnect")) {
                String message = scanner.nextLine();
                writer.println("BCST " + message);
                writer.flush();
            }

            writer.println("QUIT");
            writer.flush();
            keeper.stop();
            while (!reader.readLine().equals("+OK Goodbye")) {
                System.out.println("Disconnected from the server");
            }


        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    }

