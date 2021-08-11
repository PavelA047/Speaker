package client;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

public class RegController {
    @FXML
    private TextField loginField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private TextField nickField;
    @FXML
    private TextArea textArea;

    private Controller controller;

    public void setController(Controller controller) {
        this.controller = controller;
    }

    @FXML
    public void tryToReg(ActionEvent actionEvent) {
        String login = loginField.getText().trim();
        String pass = passwordField.getText().trim();
        String nick = nickField.getText().trim();

        if (login.equals("") || pass.equals("") || nick.equals("")) {
            textArea.appendText("Fill log, pass, nick\n");
            return;
        }

        if (login.contains(" ") || pass.contains(" ") || nick.contains(" ")) {
            textArea.appendText("Fill log, pass, nick without space\n");
            return;
        }
        controller.registration(login, pass, nick);
    }

    public void regResult(String msg) {
        textArea.appendText(msg + "\n");
    }
}
