import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class ClientHandling implements Runnable {

    private final Socket socket;
    private final BufferedReader reader;
    private final BufferedWriter writer;
    private String username = "Anonymous";

    public ClientHandling(Socket socket) throws IOException {
        this.socket = socket;

        reader = new BufferedReader(
                new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));

        writer = new BufferedWriter(
                new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8));
    }

    @Override
    public void run() {
        try {
            String message;

            while ((message = reader.readLine()) != null) {

                if (message.startsWith("/name ")) {
                    String newName = message.substring(6).trim();

                    boolean ok = Chatroom.registerUsername(newName, this);

                    if (ok) {
                        username = newName;
                        sendMessage("Username set to " + username);
                    } else {
                        sendMessage("Username already taken");
                    }
                }

                else if (message.startsWith("/send ")) {
                    String[] parts = message.split(" ", 3);

                    if (parts.length < 3) {
                        sendMessage("Usage: /send <username> <filename>");
                        continue;
                    }

                    String targetUser = parts[1];
                    String filename = parts[2];

                    ClientHandling target = Chatroom.getUser(targetUser);

                    if (target == null) {
                        sendMessage("User not found: " + targetUser);
                        continue;
                    }

                    File file = new File(filename);
                    if (!file.exists()) {
                        sendMessage("File not found: " + filename);
                        continue;
                    }

                    long fileSize = file.length();

                    // Server only signals the receiver.
                    target.sendMessage("FILE_OFFER " + username + " " + file.getName() + " " + fileSize);
                    sendMessage("File offer sent to " + targetUser + ": " + file.getName());
                }

                else if (message.startsWith("FILE_READY ")) {

                    // FILE_READY <senderUsername>
                    String[] parts = message.split(" ", 3);

                    if (parts.length < 3) {
                        sendMessage("Invalid FILE_READY message.");
                        continue;
                    }

                    String senderUsername = parts[1];
                    String port = parts[2];

                    ClientHandling sender = Chatroom.getUser(senderUsername);

                    if (sender != null) {
                        // Tell sender the receiver's IP + port.
                        String receiverIp = socket.getInetAddress().getHostAddress();
                        sender.sendMessage("FILE_READY " + username + " " + receiverIp + " " + port);
                    }
                }

                else if (message.startsWith("FILE_REJECT ")) {

                    // FILE_REJECT
                    String[] parts = message.split(" ", 2);

                    if (parts.length < 2) {
                        sendMessage("Invalid FILE_REJECT message.");
                        continue;
                    }

                    String senderUsername = parts[1];
                    ClientHandling sender = Chatroom.getUser(senderUsername);

                    if (sender != null) {
                        sender.sendMessage("FILE_REJECT " + username);
                    }
                }

                else {
                    Chatroom.sendMessage(username + ": " + message, this);
                }
            }

        } catch (IOException e) {
            System.out.println("Connection error");
        } finally {
            Chatroom.clientRemove(this);

            try {
                socket.close();
            } catch (IOException ignored) {
            }

            System.out.println("Client disconnected");
        }
    }

    public void sendMessage(String message) {
        try {
            writer.write(message);
            writer.newLine();
            writer.flush();
        } catch (IOException e) {
            System.out.println("Send error");
        }
    }
}