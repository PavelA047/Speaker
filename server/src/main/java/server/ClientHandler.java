package server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class ClientHandler {
    Socket socket;
    DataInputStream in;
    DataOutputStream out;
    Server server;

    private boolean authenticated;
    private String nick;

    public ClientHandler(Socket socket, Server server) {
        try {
            this.socket = socket;
            this.server = server;

            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());

            new Thread(() -> {
                try {
                    while (true) {
                        String string = in.readUTF();

                        if (string.equals("/end")) {
                            sendMessage("/end");
                            System.out.println("Client disconnected");
                            break;
                        }
                        if (string.startsWith("/auth ")) {
                            String[] token = string.split("\\s+");
                            nick = server.getAuthService().getNickByLogAndPas(token[1], token[2]);
                            if (nick != null) {
                                server.subscribe(this);
                                authenticated = true;
                                sendMessage("/authok " + nick);
                                break;
                            } else {
                                sendMessage("Incorrect login/password");
                            }
                        }
                    }

                    while (authenticated) {
                        String string = in.readUTF();

                        if (string.equals("/end")) {
                            sendMessage("/end");
                            System.out.println("Client disconnected");
                            break;
                        }
                        server.broadcastMassage(this, string);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    server.unsubscribe(this);
                    try {
                        socket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendMessage(String msg) {
        try {
            out.writeUTF(msg);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getNick() {
        return nick;
    }
}
