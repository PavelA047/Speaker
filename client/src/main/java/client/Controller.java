package client;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ListView;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.WindowEvent;

import java.io.*;
import java.net.Socket;
import java.net.URL;
import java.nio.file.Files;
import java.util.List;
import java.util.ResourceBundle;

public class Controller implements Initializable {
    @FXML
    private ListView<String> clientList;
    @FXML
    private TextField textField;
    @FXML
    private TextArea textArea;
    @FXML
    private TextField loginField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private HBox authPanel;
    @FXML
    private HBox msgPanel;

    private Socket socket;
    private final int PORT = 8189;
    private final String IP_ADDRESS = "localhost";
    private DataInputStream in;
    private DataOutputStream out;

    private boolean authenticated;
    private String nick;
    private String login;
    File file;
    private Stage stage;
    private Stage regStage;
    private RegController regController;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        Platform.runLater(() -> {
            stage = (Stage) textField.getScene().getWindow();
            stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
                @Override
                public void handle(WindowEvent event) {
                    System.out.println("bye");
                    if (socket != null && !socket.isClosed()) {
                        try {
                            out.writeUTF("/end");
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });
        });
        setAuthenticated(false);
    }

    @FXML
    public void sendMsg(ActionEvent actionEvent) {
        try {
            out.writeUTF(textField.getText());
            textField.clear();
            textField.requestFocus();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void tryToAuth(ActionEvent actionEvent) {
        if (socket == null || socket.isClosed()) {
            connect();
        }
        String log = loginField.getText().trim();
        String pass = passwordField.getText().trim();
        String msg = String.format("/auth %s %s", log, pass);

        try {
            out.writeUTF(msg);
            loginField.clear();
            passwordField.clear();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setAuthenticated(boolean authenticated) {
        this.authenticated = authenticated;
        authPanel.setVisible(!authenticated);
        authPanel.setManaged(!authenticated);
        msgPanel.setVisible(authenticated);
        msgPanel.setManaged(authenticated);
        clientList.setVisible(authenticated);
        clientList.setManaged(authenticated);

        if (!authenticated) {
            nick = "";
        }
        setTitle(nick);
        textArea.clear();
    }

    public void connect() {
        try {
            socket = new Socket(IP_ADDRESS, PORT);
            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());
            new Thread(() -> {
                try {
                    while (true) {
                        String string = in.readUTF();
                        if (string.startsWith("/")) {
                            if (string.equals("/end")) {
                                break;
                            }
                            if (string.startsWith("/authok")) {
                                String[] token = string.split("\\s", 3);
                                nick = token[1];
                                login = token[2];
                                setAuthenticated(true);
                                break;
                            }
                            if (string.equals("/regOk")) {
                                regController.regResult("Success");
                            }
                            if (string.equals("/regNo")) {
                                regController.regResult("Fail");
                            }
                        } else {
                            textArea.appendText(string + "\n");
                        }
                    }

                    while (authenticated) {
                        file = new File("client/src/main/java/client/" + login + ".txt");
                        file.createNewFile();
                        String string = in.readUTF();
                        if (string.startsWith("/")) {
                            if (string.equals("/end")) {
                                break;
                            }
                            if (string.startsWith("/changeNick")) {
                                String[] token = string.split("\\s+", 2);
                                String nickNew = token[1];
                                Platform.runLater(() -> {
                                    stage.setTitle(String.format("Speaker [ %s ]", nickNew));
                                });
                            }
                            if (string.startsWith("/clientlist ")) {
                                String[] token = string.split("\\s+");
                                Platform.runLater(() -> {
                                    clientList.getItems().clear();
                                    for (int i = 1; i < token.length; i++) {
                                        clientList.getItems().add(token[i]);
                                    }
                                });
                            }
                        } else {
                            textArea.appendText(string + "\n");
                        }
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    System.out.println("Disconnected");
                    setAuthenticated(false);
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

    private void setTitle(String nickname) {
        Platform.runLater(() -> {
            if (!nickname.equals("")) {
                stage.setTitle(String.format("Speaker [ %s ]", nickname));
            } else {
                stage.setTitle("Speaker");
            }
        });
    }

    public void clientListClick(MouseEvent mouseEvent) {
        String receiver = clientList.getSelectionModel().getSelectedItem();
        textField.setText(String.format("/w %s ", receiver));
    }

    private void createRegWindow() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/reg.fxml"));
            Parent root = fxmlLoader.load();
            regStage = new Stage();
            regStage.setTitle("Speaker registration");
            regStage.setScene(new Scene(root, 600, 400));
            regController = fxmlLoader.getController();
            regController.setController(this);

            regStage.initStyle(StageStyle.UTILITY);
            regStage.initModality(Modality.APPLICATION_MODAL);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void showRegWindow(ActionEvent actionEvent) {
        if (regStage == null) {
            createRegWindow();
        }
        regStage.show();
    }

    public void registration(String log, String pass, String nick) {
        String msg = String.format("/reg %s %s %s", log, pass, nick);
        if (socket == null || socket.isClosed()) {
            connect();
        }
        try {
            out.writeUTF(msg);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
