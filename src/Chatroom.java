import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class Chatroom {

    private static final Set<ClientHandling> clients =
            Collections.synchronizedSet(new HashSet<>());

    private static final Map<String, ClientHandling> usernames =
            new ConcurrentHashMap<>();

    // group name -> members
    private static final Map<String, Set<ClientHandling>> groups =
            new ConcurrentHashMap<>();

    public static void main(String[] args) throws IOException {
        int port = 5000;
        ServerSocket serverSocket = new ServerSocket(port);
        System.out.println("Server started on port " + port);

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

    public static ClientHandling getUser(String username) {
        return usernames.get(username);
    }

    public static void sendToUser(String username, String message) {
        ClientHandling target = usernames.get(username);
        if (target != null) {
            target.sendMessage(message);
        }
    }

    public static void joinGroup(String group, ClientHandling client) {
        groups.putIfAbsent(group, ConcurrentHashMap.newKeySet());
        groups.get(group).add(client);
    }

    public static void leaveGroup(String group, ClientHandling client) {
        Set<ClientHandling> members = groups.get(group);
        if (members != null) {
            members.remove(client);
            if (members.isEmpty()) {
                groups.remove(group);
            }
        }
    }

    public static void sendGroupMessage(String group, String message, ClientHandling sender) {
        Set<ClientHandling> members = groups.get(group);
        if (members == null) {
            return;
        }

        for (ClientHandling client : members) {
            if (client != sender) {
                client.sendMessage(message);
            }
        }
    }

    public static void clientRemove(ClientHandling client) {
        clients.remove(client);

        String nameToRemove = null;
        for (Map.Entry<String, ClientHandling> entry : usernames.entrySet()) {
            if (entry.getValue() == client) {
                nameToRemove = entry.getKey();
                break;
            }
        }

        if (nameToRemove != null) {
            usernames.remove(nameToRemove);
        }

        for (Set<ClientHandling> members : groups.values()) {
            members.remove(client);
        }
    }
}