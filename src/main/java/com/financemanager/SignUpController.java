package com.financemanager;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;
import javafx.util.converter.DoubleStringConverter;

import java.net.URL;
import java.util.ResourceBundle;

public class SignUpController implements Initializable {

    @FXML
    private Button button_new_sign_up; //pole representing sign up button
    @FXML
    private Button button_new_login; //pole representing login scene moving button

    @FXML
    private Button buttonSignUpQuit; //pole representing sign up quitting button

    @FXML
    private TextField tf_new_username; //pole representing new username textfield

    @FXML
    private PasswordField pf_new_password; //pole representing new password passwordfield

    @FXML
    private TextField tf_budget;

    @FXML
    private void handleLoginQuitButtonAction(ActionEvent event) {
        // Method closing app with buttonLoginQuit
        Stage stage = (Stage) buttonSignUpQuit.getScene().getWindow();
        stage.close();
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        button_new_sign_up.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                String username = tf_new_username.getText();
                String password = pf_new_password.getText();
                String budgetText = tf_budget.getText();
                double budget = 0.0;
                if (budgetText.trim().isEmpty()) {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setContentText("Invalid budget value. Please enter a valid number.");
                    alert.show();
                } else {
                        budget = Double.parseDouble(budgetText.replace(',', '.'));
                    if (!username.trim().isEmpty() && !password.trim().isEmpty() && password.length() >= 8 && password.matches("(?=.*\\d)(?=.*[a-zA-Z])(?!.*\\s).{8,}")) {

                        DBUtils.signUpUser(event, username, password, budget);
                    } else {
                        System.out.println("Please fill in all information or ensure that the password has a minimum of 8 characters");
                        Alert alert = new Alert(Alert.AlertType.ERROR);
                        alert.setContentText("Please fill in all information to sign up\nRemember! \nYour password must have a minimum of 8 characters including:\nMinimum 1 digit\nAt least one uppercase or lowercase letter\nCannot contain whitespace");
                        alert.show();
                    }
                }


            }
        });

        button_new_login.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                DBUtils.changeSceneDash(event, "sample.fxml", null);
            }
        });

        tf_new_username.setOnKeyPressed(this::handleKeyPress);
        pf_new_password.setOnKeyPressed(this::handleKeyPress);
        tf_budget.setOnKeyPressed(this::handleKeyPress);
    }
    private void handleKeyPress(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            String budgetText = tf_budget.getText();
            double budget = Double.parseDouble(budgetText);
            ActionEvent actionEvent = new ActionEvent(event.getSource(), event.getTarget());
            DBUtils.signUpUser(actionEvent, tf_new_username.getText(), pf_new_password.getText(), budget);
        }
    }
}
