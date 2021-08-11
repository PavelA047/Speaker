package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class Server {
    private ServerSocket server;
    private Socket socket;
    private final int PORT = 8189;
    private List<ClientHandler> clients;
    private AuthService authService;

    public Server() {
        clients = new CopyOnWriteArrayList<>();
        authService = new SimpleAuthService();
        try {
            server = new ServerSocket(PORT);
            System.out.println("Server started");

            while (true) {
                socket = server.accept();
                System.out.println("Client connected");
                new ClientHandler(socket, this);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                server.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void broadcastMassage(ClientHandler s, String msg) {
        String mess = String.format("[ %s ]: %s", s.getNick(), msg);
        for (ClientHandler c : clients) {
            c.sendMessage(mess);
        }
    }

    public void sendSpecificMsg(String sNick, String sMsg, ClientHandler clientHandler) {
        for (ClientHandler c : clients) {
            if (c.getNick().equals(sNick)) {
                String mess = String.format("[ %s ] to [ %s ] : %s", clientHandler.getNick(), sNick, sMsg);
                c.sendMessage(mess);
                if (!clientHandler.getNick().equals(sNick)) {
                    clientHandler.sendMessage(mess);
                }
                return;
            }
        }
        clientHandler.sendMessage("Not found user " + sNick);
    }

    public void subscribe(ClientHandler clientHandler) {
        clients.add(clientHandler);
        broadcastClients();
    }

    public void unsubscribe(ClientHandler clientHandler) {
        clients.remove(clientHandler);
        broadcastClients();
    }

    public AuthService getAuthService() {
        return authService;
    }

    public boolean isLoginAuthenticated(String login) {
        for (ClientHandler c : clients) {
            if (c.getLogin().equals(login)) {
                return true;
            }
        }
        return false;
    }

    public void broadcastClients() {
        StringBuilder sb = new StringBuilder("/clientlist");
        for (ClientHandler c : clients) {
            sb.append(" ").append(c.getNick());
        }
        String mess = sb.toString();
        for (ClientHandler c : clients) {
            c.sendMessage(mess);
        }
    }
}
