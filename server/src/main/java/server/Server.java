package server;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.LogManager;
import java.util.logging.Logger;

public class Server {
    private ServerSocket server;
    private Socket socket;
    private final int PORT = 8189;
    private List<ClientHandler> clients;
    private AuthService authService;
    private ExecutorService service = Executors.newCachedThreadPool();
    private Logger logger = Logger.getLogger(Server.class.getName());
    private LogManager manager = LogManager.getLogManager();

    public Server() {
        try {
            manager.readConfiguration(new FileInputStream("logging.properties"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        clients = new CopyOnWriteArrayList<>();
        authService = new DataBaseAuthService();
        try {
            server = new ServerSocket(PORT);
            logger.info("Server started");

            while (true) {
                socket = server.accept();
                logger.info("Client connected");
                new ClientHandler(socket, this, service, logger);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                DataBaseAuthService.disconnect();
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
        int myNick = authService.getIdByNick(s.getNick());
        authService.history(myNick, 888, msg);
    }

    public void sendSpecificMsg(String sNick, String sMsg, ClientHandler clientHandler) {
        for (ClientHandler c : clients) {
            if (c.getNick().equals(sNick)) {
                String mess = String.format("[ %s ] to [ %s ] : %s", clientHandler.getNick(), sNick, sMsg);
                c.sendMessage(mess);
                if (!clientHandler.getNick().equals(sNick)) {
                    clientHandler.sendMessage(mess);
                }
                int myNick = authService.getIdByNick(clientHandler.getNick());
                int recNick = authService.getIdByNick(sNick);
                authService.history(myNick, recNick, sMsg);
                return;
            }
        }
        clientHandler.sendMessage("Not found user " + sNick);
    }

    public void changeNickName(String nickOld, String nickNew) {
        authService.changeNick(nickOld, nickNew);
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
