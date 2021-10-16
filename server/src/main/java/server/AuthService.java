package server;

public interface AuthService {
    String getNickByLogAndPas(String log, String pas);

    boolean registration(String log, String pass, String nick);

    default void changeNick(String nickOld, String nickNew) {
    }

    default void history(Integer id_sender, Integer id_receiver, String message) {
    }

    default Integer getIdByNick(String nick) {
        return null;
    }

    default String getMessageByNick(String nick) {
        return null;
    }
}
