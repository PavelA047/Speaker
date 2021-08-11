package server;

public interface AuthService {
    String getNickByLogAndPas(String log, String pas);
    boolean registration(String log, String pass, String nick);
}
