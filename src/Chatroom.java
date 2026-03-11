import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Set;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class Chatroom {

    private static final Set<ClientHandling> clients =
            Collections.synchronizedSet(new HashSet<>());

    private static final Map<String, ClientHandling> usernames = new ConcurrentHashMap<>();

    public static void main(String[] args) throws IOException {
        int port = 5000;
        ServerSocket serverSocket = new ServerSocket(port);
        System.out.println("Server started on port 5000");

        while (true) {
            Socket socket = serverSocket.accept();
            System.out.println("Client connected");

            ClientHandling handler = new ClientHandling(socket);
            clients.add(handler);

            new Thread(handler).start();
        }
    }

    public static void sendMessage(String message, ClientHandling sender) {
        synchronized (clients) {
            for (ClientHandling client : clients) {
                if (client != sender) {
                    client.sendMessage(message);
                }
            }
        }
    }

    public static boolean registerUsername(String username, ClientHandling client) {
        return usernames.putIfAbsent(username, client) == null;
    }

    public static void clientRemove(ClientHandling client) {
        clients.remove(client);
    }
}