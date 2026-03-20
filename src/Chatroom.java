import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Set;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;


public class Chatroom {

    private static final Map<String, List<String>> mailboxes = new ConcurrentHashMap<>();

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

    public static ClientHandling getUser(String username) {
        return usernames.get(username);
    }

    public static void sendMail(String recipient, String message, ClientHandling sender) {
        ClientHandling target = usernames.get(recipient);

        //deliver instant when user online
        if (target != null) {
            target.sendMessage("[MAIL] " + message);
            return;
        }

        mailboxes.computeIfAbsent(recipient, k -> Collections.synchronizedList(new ArrayList<>())).add(message);
    }

    public static List<String> getMail (String username) {
        List<String> messages = mailboxes.remove(username);
        return messages !=null ? messages : new ArrayList<>();
    }

    public static void clientRemove(ClientHandling client) {

        clients.remove(client);

        usernames.values().remove(client);

        for (Set<ClientHandling> group : groups.values()) {

            group.remove(client);
        }
    }
}