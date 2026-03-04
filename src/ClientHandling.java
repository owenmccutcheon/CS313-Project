import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class ClientHandling implements Runnable {

    private Socket socket;
    private BufferedReader reader;
    private BufferedWriter writer;

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
                Chatroom.sendMessage(message, this);
            }

        } catch (IOException ignored) {
        } finally {
            Chatroom.clientRemove(this);

            try {
                socket.close();
            } catch (IOException ignored) {}

            System.out.println("Client disconnected");
        }
    }

    public void sendMessage(String message) {
        try {
            writer.write(message);
            writer.newLine();
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}