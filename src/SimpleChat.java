import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class SimpleChat {

    private static volatile boolean running = true;

    public static void main(String[] args) {

        if (args.length < 2) {
            System.out.println("Usage:");
            System.out.println("Server mode: java SimpleChat server <port>");
            System.out.println("Client mode: java SimpleChat client <host> <port>");
            return;
        }

        try {
            if (args[0].equalsIgnoreCase("client")) {
                String host = args[1];
                int port = Integer.parseInt(args[2]);
                startClient(host, port);
            } else {
                System.out.println("Invalid mode.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void startClient(String host, int port) throws IOException {
        Socket socket = new Socket(host, port);
        System.out.println("Connected to server");
        startChat(socket);
    }

    private static void startChat(Socket socket) throws IOException {

        BufferedReader reader = new BufferedReader(
                new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));

        BufferedWriter writer = new BufferedWriter(
                new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8));

        Scanner scanner = new Scanner(System.in, StandardCharsets.UTF_8);

        // Receiving thread
        Thread receiveThread = new Thread(() -> {
            try {
                String message;
                while (running && (message = reader.readLine()) != null) {
                    System.out.print("\r");
                    System.out.println(message);
                    System.out.print("You: ");
                }
            } catch (IOException e) {
                if (running) {
                    System.out.println("Connection closed.");
                }
            } finally {
                running = false;
            }
        });

        receiveThread.start();

        // Sending loop
        while (running) {
            System.out.print("You: ");
            String message = scanner.nextLine();

            if (message.equalsIgnoreCase("/quit")) {
                running = false;
                socket.close();
                break;
            }

            writer.write(message);
            writer.newLine();
            writer.flush();
        }

        scanner.close();
    }
}