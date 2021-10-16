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

    @Override
    public void history(Integer id_sender, Integer id_receiver, String message) {
        try {
            PreparedStatement pstmt = connection.prepareStatement("INSERT INTO history (id_sender, id_receiver, " +
                    "message) VALUES (?, ?, ?)");
            pstmt.setInt(1, id_sender);
            pstmt.setInt(2, id_receiver);
            pstmt.setString(3, message);
            pstmt.execute();
        } catch (SQLException troubles) {
            troubles.printStackTrace();
        }
    }

    @Override
    public Integer getIdByNick(String nick) {
        try {
            PreparedStatement pstmt = connection.prepareStatement("SELECT id FROM users WHERE nick = ?");
            pstmt.setString(1, nick);
            ResultSet rs = pstmt.executeQuery();
            return rs.getInt(1);
        } catch (SQLException troubles) {
            troubles.printStackTrace();
        }
        return null;
    }

    @Override
    public String getMessageByNick(String nick) {
        StringBuilder sb = new StringBuilder();
        try {
            PreparedStatement pstmt = connection.prepareStatement("SELECT (SELECT nick FROM users WHERE id = id_sender), " +
                    "(SELECT nick FROM users WHERE id = id_receiver), " +
                    "message " +
                    "FROM history " +
                    "WHERE id_sender = (SELECT id FROM users WHERE nick = ?) " +
                    "OR id_receiver = (SELECT id FROM users WHERE nick = ?) " +
                    "OR id_receiver = 888");
            pstmt.setString(1, nick);
            pstmt.setString(2, nick);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                String sender = rs.getString(1);
                String receiver = rs.getString(2);
                String text = rs.getString(3);
                sb.append(String.format("[ %s ] to [ %s ]: %s\n", sender, receiver, text));
            }
            rs.close();
        } catch (SQLException troubles) {
            troubles.printStackTrace();
        }
        return sb.toString();
    }
}
