import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class ClientHandling implements Runnable {

    private Socket socket;
    private BufferedReader reader;
    private BufferedWriter writer;
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
                        writer.write("Username set to " + username);
                    } else {
                        writer.write("Username already taken");
                    }

                    writer.newLine();
                    writer.flush();
                }

                else if (message.startsWith("/send ")) {
                    String filename = message.substring(6).trim();
                    File file = new File(filename);

                    if (!file.exists()) {
                        writer.write("File not found");
                        writer.newLine();
                        writer.flush();
                        continue;
                    }

                    FileInputStream fis = new FileInputStream(file);
                    ByteArrayOutputStream bos = new ByteArrayOutputStream();

                    byte[] buffer = new byte[4096];
                    int bytesRead;

                    while ((bytesRead = fis.read(buffer)) != -1) {
                        bos.write(buffer, 0, bytesRead);
                    }

                    fis.close();

                    byte[] fileData = bos.toByteArray();

                    Chatroom.sendFile(file.getName(), fileData, this);

                    writer.write("File sent: " + file.getName());
                    writer.newLine();
                    writer.flush();
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

    public void sendFile(String filename, byte[] data) {
        try {
            String encoded = Base64.getEncoder().encodeToString(data);

            writer.write("FILE " + filename + " " + encoded);
            writer.newLine();
            writer.flush();
        } catch (IOException e) {
            System.out.println("Send file error");
        }
    }
}