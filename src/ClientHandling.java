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

                    username = message.substring(6);
                    sendMessage("Username set to " + username);

                }

                else if (message.startsWith("/join ")) {

                    currentGroup = message.substring(6);
                    Chatroom.joinGroup(currentGroup, this);

                    sendMessage("Joined group " + currentGroup);

                }

                else if (currentGroup != null) {

                    Chatroom.sendGroupMessage(
                            currentGroup,
                            "[" + currentGroup + "] " + username + ": " + message
                    );

                }

            }

        } catch (IOException ignored) {
        }
    }

    public void sendMessage(String message) {

        try {

            writer.write(message);
            writer.newLine();
            writer.flush();

        } catch (IOException ignored) {}
    }
}