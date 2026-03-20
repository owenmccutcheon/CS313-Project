import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class ClientHandling implements Runnable {

    private Socket socket;
    private BufferedReader reader;
    private BufferedWriter writer;

    private String username = "Anonymous";
    private String currentGroup = null;

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

                    boolean success = Chatroom.registerUsername(newName, this);

                    if (success) {

                        username = newName;

                        writer.write("Username set to " + username);
                        writer.newLine();
                        writer.flush();

                        //Delivering mail
                        for (String msg : Chatroom.getMail(username)) {
                            writer.write("[MAIL] " + msg);
                        }
                    } else {

                        writer.write("Username already taken");
                    }

                    writer.newLine();
                    writer.flush();
                }

                else if (message.startsWith("/join ")) {

                    String group = message.substring(6).trim();

                    Chatroom.joinGroup(group, this);

                    currentGroup = group;

                    writer.write("Joined group: " + group);
                    writer.newLine();
                    writer.flush();
                }
                else if (message.startsWith("/mail ")) {
                    String[] parts = message.split(" ", 3);
                    if(parts.length < 3) {
                        writer.write("Usage: /mail <user> <message>");
                        writer.newLine();
                        writer.flush();
                        continue;
                    }

                    String target = parts[1];
                    String msg = parts[2];

                    Chatroom.sendMail(target, username + ": " + msg, this);

                    writer.write("Mail sent to " + target);
                    writer.newLine();
                    writer.flush();
                }
                else {

                    if (currentGroup != null) {

                        Chatroom.sendGroupMessage(currentGroup,
                                username + ": " + message, this);

                    } else {

                        Chatroom.sendMessage(username + ": " + message, this);
                    }
                }
            }

        } catch (IOException ignored) {}

        finally {

            Chatroom.clientRemove(this);

            try { socket.close(); } catch (IOException ignored) {}

            System.out.println("Client disconnected");
        }
    }

    public synchronized void sendMessage(String message) {

        try {

            writer.write(message);
            writer.newLine();
            writer.flush();

        } catch (IOException e) {

            e.printStackTrace();
        }
    }
}