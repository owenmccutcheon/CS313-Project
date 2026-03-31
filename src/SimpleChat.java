import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.ConcurrentHashMap;

public class SimpleChat {

    private static volatile boolean running = true;
    private static BufferedWriter serverWriter;

    // pending outgoing files: receiver username -> file
    private static final Map<String, File> pendingFiles = new ConcurrentHashMap<>();

    public static void main(String[] args) {

        if (args.length < 3) {
            System.out.println("Usage:");
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

        serverWriter = new BufferedWriter(
                new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8));

        Scanner scanner = new Scanner(System.in, StandardCharsets.UTF_8);

        Thread receiveThread = new Thread(() -> {
            try {
                String message;

                while (running && (message = reader.readLine()) != null) {

                    if (message.startsWith("FILE_OFFER ")) {
                        handleFileOffer(message);
                    }

                    else if (message.startsWith("FILE_READY ")) {
                        handleFileReady(message);
                    }

                    else if (message.startsWith("FILE_REJECT ")) {
                        String[] parts = message.split(" ", 2);
                        if (parts.length == 2) {
                            System.out.print("\r");
                            System.out.println(parts[1] + " rejected your file transfer.");
                            System.out.print("You: ");
                        }
                    }

                    else {
                        System.out.print("\r");
                        System.out.println(message);
                        System.out.print("You: ");
                    }
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

        while (running) {
            System.out.print("You: ");
            String message = scanner.nextLine();

            if (message.equalsIgnoreCase("/quit")) {
                running = false;
                socket.close();
                break;
            }


            if (message.startsWith("/send ")) {
                String[] parts = message.split(" ", 3);

                if (parts.length < 3) {
                    System.out.println("Usage: /send <username> <filename>");
                    continue;
                }

                String targetUser = parts[1];
                String filename = parts[2];

                File file = new File(filename);

                if (!file.exists()) {
                    System.out.println("File not found: " + filename);
                    continue;
                }

                pendingFiles.put(targetUser, file);
            }

            serverWriter.write(message);
            serverWriter.newLine();
            serverWriter.flush();
        }

        scanner.close();
    }

    private static void handleFileOffer(String message) {
        // FILE_OFFER
        String[] parts = message.split(" ", 4);

        if (parts.length < 4) {
            System.out.print("\r");
            System.out.println("Invalid file offer received.");
            System.out.print("You: ");
            return;
        }

        String sender = parts[1];
        String filename = parts[2];
        String size = parts[3];

        System.out.print("\r");
        System.out.println(sender + " wants to send " + filename + " (" + size + " bytes)");



        try {
            ServerSocket tempFileServer = new ServerSocket(0);
            int port = tempFileServer.getLocalPort();

            // Tell sender, via server, that we are ready.
            serverWriter.write("FILE_READY " + sender + " " + port);
            serverWriter.newLine();
            serverWriter.flush();

            new Thread(() -> receiveFile(tempFileServer)).start();

            System.out.println("Accepted file transfer from " + sender);
            System.out.print("You: ");

        } catch (IOException e) {
            try {
                serverWriter.write("FILE_REJECT " + sender);
                serverWriter.newLine();
                serverWriter.flush();
            } catch (IOException ignored) {
            }

            System.out.print("\r");
            System.out.println("Failed to prepare for file transfer.");
            System.out.print("You: ");
        }
    }

    private static void handleFileReady(String message) {
        // FILE_READY
        String[] parts = message.split(" ", 4);

        if (parts.length < 4) {
            System.out.print("\r");
            System.out.println("Invalid FILE_READY message.");
            System.out.print("You: ");
            return;
        }

        String receiverUsername = parts[1];
        String ip = parts[2];
        int port = Integer.parseInt(parts[3]);

        File file = pendingFiles.remove(receiverUsername);

        if (file == null) {
            System.out.print("\r");
            System.out.println("No pending file found for " + receiverUsername);
            System.out.print("You: ");
            return;
        }

        new Thread(() -> sendFile(ip, port, file)).start();
    }

    private static void sendFile(String ip, int port, File file) {
        try (Socket fileSocket = new Socket(ip, port);
             DataOutputStream dos = new DataOutputStream(fileSocket.getOutputStream());
             FileInputStream fis = new FileInputStream(file)) {

            dos.writeUTF(file.getName());
            dos.writeLong(file.length());

            byte[] buffer = new byte[4096];
            int bytesRead;

            while ((bytesRead = fis.read(buffer)) != -1) {
                dos.write(buffer, 0, bytesRead);
            }

            dos.flush();

            System.out.print("\r");
            System.out.println("File sent: " + file.getName());
            System.out.print("You: ");

        } catch (IOException e) {
            System.out.print("\r");
            System.out.println("File send failed: " + e.getMessage());
            System.out.print("You: ");
        }
    }

    private static void receiveFile(ServerSocket tempFileServer) {
        try (ServerSocket server = tempFileServer;
             Socket incoming = server.accept();
             DataInputStream dis = new DataInputStream(incoming.getInputStream())) {

            String filename = dis.readUTF();
            long size = dis.readLong();

            try (FileOutputStream fos = new FileOutputStream("received_" + filename)) {
                byte[] buffer = new byte[4096];
                long remaining = size;

                while (remaining > 0) {
                    int read = dis.read(buffer, 0, (int) Math.min(buffer.length, remaining));
                    if (read == -1) {
                        break;
                    }
                    fos.write(buffer, 0, read);
                    remaining -= read;
                }
            }

            System.out.print("\r");
            System.out.println("File received: received_" + filename);
            System.out.print("You: ");

        } catch (IOException e) {
            System.out.print("\r");
            System.out.println("File receive failed: " + e.getMessage());
            System.out.print("You: ");
        }
    }
}