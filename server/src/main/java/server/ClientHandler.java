package server;

import java.io.*;
import java.net.Socket;
import java.net.SocketTimeoutException;

public class ClientHandler {
    Socket socket;
    DataInputStream in;
    DataOutputStream out;
    Server server;

    private boolean authenticated;
    private String nick;
    private String login;

    public ClientHandler(Socket socket, Server server) {
        try {
            this.socket = socket;
            this.server = server;

            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());

//            new Thread(() -> {
                try {
                    socket.setSoTimeout(120000);
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
                            login = token[1];
                            if (nick != null) {
                                if (!server.isLoginAuthenticated(login)) {
                                    sendMessage("/authok " + nick + " " + login);
                                    server.subscribe(this);
                                    authenticated = true;
//                                    sendMessage(server.getAuthService().getMessageByNick(nick));
                                    socket.setSoTimeout(0);
                                    break;
                                } else {
                                    sendMessage("This login is busy");
                                }
                            } else {
                                sendMessage("Incorrect login/password");
                            }
                        }
                        if (string.startsWith("/reg")) {
                            String[] token = string.split("\\s+");
                            if (token.length < 4) {
                                continue;
                            }
                            boolean regOk = server.getAuthService().registration(token[1], token[2],
                                    token[3]);
                            if (regOk) {
                                sendMessage("/regOk");
                            } else {
                                sendMessage("/regNo");
                            }
                        }
                    }

                    while (authenticated) {
                        String string = in.readUTF();

                        if (string.startsWith("/")) {
                            if (string.equals("/end")) {
                                sendMessage("/end");
                                System.out.println("Client disconnected");
                                break;
                            }
                            if (string.startsWith("/w")) {
                                String[] token = string.split("\\s+", 3);
                                if (token.length < 3) {
                                    continue;
                                }
                                String specificMsg = token[2];
                                String specificNick = token[1];
                                server.sendSpecificMsg(specificNick, specificMsg, this);
                            }
                            if (string.startsWith("/changeNick")) {
                                String[] token = string.split("\\s+", 3);
                                String nickOld = token[1];
                                String nickNew = token[2];
                                server.changeNickName(nickOld, nickNew);
                                out.writeUTF("/changeNick " + nickNew);
                                this.nick = nickNew;
                                server.broadcastClients();
                            }
                        } else {
                            server.broadcastMassage(this, string);
                        }
                    }
                } catch (SocketTimeoutException e) {
                    sendMessage("/end");
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
//            }).start();
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

    public String getLogin() {
        return login;
    }
}
