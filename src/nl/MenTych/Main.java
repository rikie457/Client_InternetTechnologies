package nl.MenTych;

import java.util.Scanner;

public class Main {


    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        String username;

        do {
            System.out.print("Insert your username: ");
            username = scanner.nextLine();
        } while (!username.matches("(\\w)\\w+"));
        Client client = new Client("127.0.0.1", 1337, username);

    }
}
