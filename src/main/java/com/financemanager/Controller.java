package com.financemanager;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ResourceBundle;

public class Controller implements Initializable {
    @FXML
    private Button buttonLoginQuit; //pole representing quit login button

    @FXML
    private void handleLoginQuitButtonAction(ActionEvent event) {
        // Method closing app with buttonLoginQuit
        Stage stage = (Stage) buttonLoginQuit.getScene().getWindow();
        stage.close();
    }

    @FXML
    private Button button_login; //pole representing login button

    @FXML
    private Button button_sign_up; //pole representing sign up scene moving button

    @FXML
    private TextField tf_username; //pole representing username textfield

    @FXML
    private PasswordField pf_password; //pole representing password passwordfield

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        button_login.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                DBUtils.logInUser(event, tf_username.getText(), pf_password.getText());
            }
        });

        button_sign_up.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                DBUtils.changeSceneDash(event, "sign-up.fxml", null);

            }
        });

        tf_username.setOnKeyPressed(this::handleKeyPress);
        pf_password.setOnKeyPressed(this::handleKeyPress);
    }

    @FXML
    private void handleKeyPress(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            ActionEvent actionEvent = new ActionEvent(event.getSource(), event.getTarget());
            DBUtils.logInUser(actionEvent, tf_username.getText(), pf_password.getText());
        }
    }
}
