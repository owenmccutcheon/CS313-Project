import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Set;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

public class Chatroom {

    private static final Set<ClientHandling> clients =
            Collections.synchronizedSet(new HashSet<>());

    private static final Map<String, ClientHandling> usernames =
            new ConcurrentHashMap<>();

    // THREAD SAFE GROUP STORAGE
    private static final Map<String, Set<ClientHandling>> groups =
            new ConcurrentHashMap<>();

    public static void main(String[] args) throws IOException {

        ServerSocket serverSocket = new ServerSocket(5000);
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

    // THREAD SAFE GROUP JOIN
    public static void joinGroup(String group, ClientHandling client) {

        groups.putIfAbsent(group, new CopyOnWriteArraySet<>());

        groups.get(group).add(client);
    }


    public static void sendGroupMessage(String group, String message, ClientHandling sender) {
        Set<ClientHandling> members = groups.get(group);
        if (members == null) return;

        for (ClientHandling client : members) {
            if (client != sender) {
                client.sendMessage(message);
            }
        }
    }

    public static void clientRemove(ClientHandling client) {

        clients.remove(client);

        for (Set<ClientHandling> group : groups.values()) {

            group.remove(client);
        }
    }
}