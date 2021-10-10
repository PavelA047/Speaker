package server;

public interface AuthService {
    String getNickByLogAndPas(String log, String pas);

    boolean registration(String log, String pass, String nick);

    default void changeNick(String nickOld, String nickNew) {
    }
}
