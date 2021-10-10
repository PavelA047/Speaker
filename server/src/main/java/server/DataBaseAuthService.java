package server;

import java.sql.*;

public class DataBaseAuthService implements AuthService {
    private static Connection connection;

    static {
        try {
            connect();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void connect() throws SQLException {
        connection = DriverManager.getConnection("jdbc:sqlite:main.db");
    }

    public static void disconnect() {
        try {
            if (connection != null) {
                connection.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String getNickByLogAndPas(String log, String pas) {
        try {
            PreparedStatement pstmt = connection.prepareStatement("SELECT nick FROM users WHERE login = ?" +
                    "AND password = ?");
            pstmt.setString(1, log);
            pstmt.setString(2, pas);
            ResultSet rs = pstmt.executeQuery();
            return rs.getString(1);
        } catch (SQLException ignored) {
        }
        return null;
    }

    @Override
    public boolean registration(String log, String pass, String nick) {
        try {
            PreparedStatement pstmt = connection.prepareStatement("INSERT INTO users (login, nick, password)" +
                    "VALUES (?, ?, ?)");
            pstmt.setString(1, log);
            pstmt.setString(2, nick);
            pstmt.setString(3, pass);
            pstmt.execute();
        } catch (SQLException troubles) {
            return false;
        }
        return true;
    }

    @Override
    public void changeNick(String nickOld, String nickNew) {
        try {
            PreparedStatement pstmt = connection.prepareStatement("UPDATE users SET nick = ? WHERE nick = ?");
            pstmt.setString(1, nickNew);
            pstmt.setString(2, nickOld);
            pstmt.execute();
        } catch (SQLException troubles) {
            troubles.printStackTrace();
        }
    }
}
