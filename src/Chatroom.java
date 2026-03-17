import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

public class Chatroom {

    private static final Set<ClientHandling> clients = new HashSet<>();

    private static final Map<String, Set<ClientHandling>> groups = new HashMap<>();

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

    public static void joinGroup(String group, ClientHandling client) {

        if (!groups.containsKey(group)) {
            groups.put(group, new HashSet<>());
        }

        groups.get(group).add(client);
    }

    public static void sendGroupMessage(String group, String message) {

        Set<ClientHandling> members = groups.get(group);

        if (members == null) return;

        for (ClientHandling client : members) {


            client.sendMessage(message);
        }
    }

    public static void removeClient(ClientHandling client) {
        clients.remove(client);
    }
}