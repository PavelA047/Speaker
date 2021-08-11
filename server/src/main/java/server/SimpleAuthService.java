package server;

import java.util.ArrayList;
import java.util.List;

public class SimpleAuthService implements AuthService {

    private class UserData {
        String login;
        String password;
        String nick;

        public UserData(String login, String password, String nick) {
            this.login = login;
            this.password = password;
            this.nick = nick;
        }
    }

    private List<UserData> users;

    public SimpleAuthService() {
        this.users = new ArrayList<>();
        users.add(new UserData("qwe", "qwe", "qwe"));
        users.add(new UserData("asd", "asd", "asd"));
        users.add(new UserData("zxc", "zxc", "zxc"));
        for (int i = 1; i < 10; i++) {
            users.add(new UserData("login " + i, "pass " + i, "nick " + i));
        }
    }

    @Override
    public String getNickByLogAndPas(String log, String pas) {
        for (UserData u : users) {
            if (u.login.equals(log) && u.password.equals(pas)) {
                return u.nick;
            }
        }
        return null;
    }

    @Override
    public boolean registration(String log, String pass, String nick) {
        for (UserData u : users) {
            if (u.login.equals(log) && u.nick.equals(nick)) {
                return false;
            }
        }
        users.add(new UserData(log, pass, nick));
        return true;
    }
}
