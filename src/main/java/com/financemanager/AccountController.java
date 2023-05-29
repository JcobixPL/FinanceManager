package com.financemanager;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;

import java.net.URL;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.ResourceBundle;

public class AccountController implements Initializable {

    public static String password;
    public static String new_username1;
    public static String new_password1;
    public boolean usernameExists = false;
    public static String username;

    public static Integer id;
    public void setUsernameDB(String username) { this.username = username; }
    public void setPasswordDB(String password) { this.password = password; }

    public void getUserId(Integer id){
        this.id = id;
    }

    @FXML
    private Button button_delete_account;

    @FXML
    private Label label_user; //pole representing label with username on main page

    @FXML
    private Label label_username; //pole representing label with username on main page

    @FXML
    private Button button_logout; //pole representing menu logout button

    @FXML
    private Button button_dashboard; //pole representing menu dashboard button

    @FXML
    private Button button_payments; //pole representing menu payments button

    @FXML
    private Button button_account; //pole representing menu account button

    @FXML
    private Button button_change_username;

    @FXML
    private Button button_change_password;

    @FXML
    private CheckBox checkbox_show_password;

    @FXML
    private Label label_password;

    @FXML
    private Label label_date_time; //pole representing actual date and time refreshing in 1 second

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        Thread updateTimeThread = new Thread(() -> {
            while (true) {
                LocalDateTime currentDateTime = LocalDateTime.now();
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                String formattedDateTime = currentDateTime.format(formatter);

                // Aktualizacja etykiety na wątku JavaFX
                javafx.application.Platform.runLater(() -> label_date_time.setText(formattedDateTime));

                try {
                    Thread.sleep(1000); // Czekaj przez 1 sekundę
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        updateTimeThread.setDaemon(true);
        updateTimeThread.start();

        checkbox_show_password.selectedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                label_password.setText(password);
            } else {
                label_password.setText("**********");
            }
        });

        button_payments.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                DBUtils.changeSceneDash(event, "payments.fxml", username);
            }
        });

        button_account.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                DBUtils.changeSceneDash(event, "account.fxml", username);
            }
        });

        button_dashboard.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                DBUtils.changeSceneDash(event, "dashboard.fxml", username);
            }
        });

        button_logout.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                DBUtils.changeSceneDash(event, "sample.fxml",  null);
            }
        });

        button_change_username.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                // Tworzenie kontrolek
                TextField newUsername = new TextField();
                // Ustawianie siatki
                GridPane gridPane = new GridPane();
                gridPane.setHgap(10);
                gridPane.setVgap(10);
                gridPane.setPadding(new Insets(10));

                gridPane.add(new Label("New username:"), 0, 0);
                gridPane.add(newUsername, 1, 1, 2, 1);

                // Walidacja pól i blokowanie przycisku "OK"
                ButtonType okButtonType = new ButtonType("OK", ButtonBar.ButtonData.OK_DONE);
                Dialog<ButtonType> dialog = new Dialog<>();
                dialog.setTitle("Changing username");
                dialog.getDialogPane().setContent(gridPane);
                dialog.getDialogPane().getButtonTypes().addAll(okButtonType, ButtonType.CANCEL);

                Node okButton = dialog.getDialogPane().lookupButton(okButtonType);
                okButton.setDisable(true);

                okButton.addEventFilter(ActionEvent.ACTION, e ->{
                    String new_username = newUsername.getText();
                    if (new_username.isEmpty()) {
                        Alert alert = new Alert(Alert.AlertType.ERROR);
                        alert.setTitle("Błąd");
                        alert.setHeaderText(null);
                        alert.setContentText("Wypełnij poprawnie wszystkie pola!");
                        alert.showAndWait();
                        e.consume();
                    }
                    else {

                        try {
                            // Utwórz połączenie do bazy danych
                            Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/javafx-project", "root", "Kubizon2003");
                            String sqlQuery = "SELECT COUNT(*) FROM users WHERE username = ?";
                            PreparedStatement statement = connection.prepareStatement(sqlQuery);
                            statement.setString(1, new_username);
                            ResultSet resultSet = statement.executeQuery();
                            if(resultSet.next()){
                                int count = resultSet.getInt(1);
                                usernameExists = count > 0;
                            }

                            new_username1 = new_username;
                            // Zamykanie zasobów
                            resultSet.close();
                            statement.close();
                            connection.close();
                        } catch (SQLException ex) {
                            ex.printStackTrace();
                            // Obsługa błędów związanych z bazą danych
                        }


                    }
                });
                // Blokowanie przycisku "OK" w zależności od wprowadzonych danych
                newUsername.textProperty().addListener((observable, oldValue, newValue) -> {
                    boolean disable = newValue.isEmpty() || newUsername.getText().isEmpty();
                    okButton.setDisable(disable);
                });

                // Wyświetlanie okna dialogowego
                dialog.showAndWait().ifPresent(result -> {
                    if (result == okButtonType) {
                        if(!usernameExists) {
                            try {
                                // Utwórz połączenie do bazy danych
                                Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/javafx-project", "root", "Kubizon2003");

                                // Zapytanie SQL do wstawienia nowego wiersza
                                String sqlQuery = "UPDATE users SET username = ? WHERE user_id = ?";

                                // Przygotuj zapytanie
                                PreparedStatement statement = connection.prepareStatement(sqlQuery);

                                // Ustaw wartości parametrów zapytania
                                statement.setString(1, newUsername.getText());
                                statement.setInt(2, id);

                                // Wykonaj zapytanie
                                int rowsInserted = statement.executeUpdate();

                                // Zamknij połączenie i zasoby
                                statement.close();
                                connection.close();
                            } catch (SQLException ex) {
                                ex.printStackTrace();
                            }
                        }
                        else {
                            Alert alert = new Alert(Alert.AlertType.ERROR);
                            alert.setTitle("Błąd");
                            alert.setHeaderText(null);
                            alert.setContentText("This username is taken!");
                            alert.showAndWait();
                        }
                        username = new_username1;
                        DBUtils.changeSceneDash(event, "account.fxml", username);
                    } else {
                        DBUtils.changeSceneDash(event, "account.fxml", username);
                    }
                });
            }
        });

        button_change_password.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                TextField newPassword = new TextField();
                GridPane gridPane = new GridPane();
                gridPane.setHgap(10);
                gridPane.setVgap(10);
                gridPane.setPadding(new Insets(10));

                gridPane.add(new Label("New password:"), 0, 0);
                gridPane.add(newPassword, 1, 1, 2, 1);

                // Walidacja pól i blokowanie przycisku "OK"
                ButtonType okButtonType = new ButtonType("OK", ButtonBar.ButtonData.OK_DONE);
                Dialog<ButtonType> dialog = new Dialog<>();
                dialog.setTitle("Changing password");
                dialog.getDialogPane().setContent(gridPane);
                dialog.getDialogPane().getButtonTypes().addAll(okButtonType, ButtonType.CANCEL);

                Node okButton = dialog.getDialogPane().lookupButton(okButtonType);
                okButton.setDisable(true);

                okButton.addEventFilter(ActionEvent.ACTION, e ->{
                    String new_password = newPassword.getText();
                    new_password1 = new_password;
                    if(new_password.isEmpty() || new_password.length() < 8 || !new_password.matches("(?=.*\\d)(?=.*[a-zA-Z])(?!.*\\s).{8,}")) {
                        Alert alert = new Alert((Alert.AlertType.ERROR));
                        alert.setTitle("Błąd");
                        alert.setHeaderText(null);
                        alert.setContentText("Please fill in all information to sign up\nRemember! \nYour new password must have a minimum of 8 characters including:\nMinimum 1 digit\nAt least one uppercase or lowercase letter\nCannot contain whitespace");
                        alert.showAndWait();
                        e.consume();
                    }
                    else if(new_password1.equals(password)){
                        Alert alert = new Alert((Alert.AlertType.ERROR));
                        alert.setTitle("Błąd");
                        alert.setHeaderText(null);
                        alert.setContentText("Warning! Your new password cannot be like your old password");
                        alert.showAndWait();
                        e.consume();
                    }
                });
                newPassword.textProperty().addListener((observable, oldValue, newValue) -> {
                    boolean disable = newValue.isEmpty() || newPassword.getText().isEmpty();
                    okButton.setDisable(disable);
                });

                // Wyświetlanie okna dialogowego
                dialog.showAndWait().ifPresent(result -> {
                    if (result == okButtonType) {
                            try {
                                // Utwórz połączenie do bazy danych
                                Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/javafx-project", "root", "Kubizon2003");

                                // Zapytanie SQL do wstawienia nowego wiersza
                                String sqlQuery = "UPDATE users SET password = ? WHERE user_id = ?";

                                // Przygotuj zapytanie
                                PreparedStatement statement = connection.prepareStatement(sqlQuery);

                                // Ustaw wartości parametrów zapytania
                                statement.setString(1, newPassword.getText());
                                statement.setInt(2, id);

                                // Wykonaj zapytanie
                                int rowsInserted = statement.executeUpdate();

                                // Zamknij połączenie i zasoby
                                statement.close();
                                connection.close();
                            } catch (SQLException ex) {
                                ex.printStackTrace();
                            }
                            password = new_password1;

                        DBUtils.changeSceneDash(event, "account.fxml", username);
                    } else {
                        DBUtils.changeSceneDash(event, "account.fxml", username);
                    }
                });
            }
        });

        button_delete_account.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {

                try{
                    Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/javafx-project", "root", "Kubizon2003");
                    String query = "SELECT user_id FROM users WHERE username = ?";
                    PreparedStatement statement = connection.prepareStatement(query);
                    statement.setString(1, username);
                    ResultSet resultSet = statement.executeQuery();
                    if (resultSet.next()) {
                        id = resultSet.getInt(1);
                    }
                    resultSet.close();
                    statement.close();
                    connection.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }

                TextInputDialog passwordDialog = new TextInputDialog();
                passwordDialog.setTitle("Confirming account deletion");
                passwordDialog.setHeaderText(null);
                passwordDialog.setContentText("Are you sure you want to delete your account? Enter your password to proceed:");
                Optional<String> passwordResult = passwordDialog.showAndWait();
                passwordResult.ifPresent(password -> {
                    // Sprawdzanie poprawności hasła
                    if (checkPassword(password)) {
                        // Hasło jest poprawne, kontynuuj usuwanie konta
                        Alert confirmationAlert = new Alert(Alert.AlertType.CONFIRMATION);
                        confirmationAlert.setTitle("Confirming account deletion");
                        confirmationAlert.setHeaderText(null);
                        confirmationAlert.setContentText("This operation cannot be undone. Are you sure you want to proceed?");

                        Optional<ButtonType> result = confirmationAlert.showAndWait();
                        result.ifPresent(buttonType -> {
                            if (buttonType == ButtonType.OK) {
                                // Usuwanie konta
                                try {
                                    Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/javafx-project", "root", "Kubizon2003");
                                    String deleteQuery = "DELETE FROM transactions WHERE user_id = ?";
                                    PreparedStatement statement = connection.prepareStatement(deleteQuery);

                                    statement.setInt(1, id);
                                    statement.executeUpdate();
                                } catch (SQLException e1) {
                                    e1.printStackTrace();
                                }

                                try {
                                    Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/javafx-project", "root", "Kubizon2003");
                                    String deleteQuery = "DELETE FROM budgets WHERE user_id = ?";
                                    PreparedStatement statement = connection.prepareStatement(deleteQuery);

                                    statement.setInt(1, id);
                                    statement.executeUpdate();
                                } catch (SQLException e1) {
                                    e1.printStackTrace();
                                }

                                try (Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/javafx-project", "root", "Kubizon2003")) {
                                    String deleteQuery = "DELETE FROM users WHERE user_id = ?";

                                    try (PreparedStatement statement = connection.prepareStatement(deleteQuery)) {
                                        statement.setInt(1, id);
                                        statement.executeUpdate();
                                    } catch (SQLException e) {
                                        e.printStackTrace();
                                    }

                                    // Wykonaj inne czynności po usunięciu konta, na przykład wyświetl komunikat potwierdzający
                                    Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
                                    successAlert.setTitle("Success");
                                    successAlert.setHeaderText(null);
                                    successAlert.setContentText("Your account has been deleted successfully!");
                                    successAlert.showAndWait();

                                    DBUtils.changeSceneDash(event, "sample.fxml", null);
                                } catch (SQLException e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                    } else {
                        // Nieprawidłowe hasło, wyświetlenie komunikatu
                        Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                        errorAlert.setTitle("Invalid Password");
                        errorAlert.setHeaderText(null);
                        errorAlert.setContentText("Invalid password. Account deletion canceled.");
                        errorAlert.showAndWait();
                    }
                });

            }
        });
    }

    private boolean checkPassword(String password) {
        try (Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/javafx-project", "root", "Kubizon2003")) {
            String selectQuery = "SELECT password FROM users WHERE user_id = ?";
            try (PreparedStatement statement = connection.prepareStatement(selectQuery)) {
                statement.setInt(1, id);
                ResultSet resultSet = statement.executeQuery();
                if (resultSet.next()) {
                    String savedPassword = resultSet.getString("password");
                    // Porównanie zapisanego hasła z wprowadzonym hasłem
                    return true;
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false; // Jeśli nie znaleziono użytkownika o podanym id
    }


    public void setUserInformation(String username) {
        label_user.setText(username.toUpperCase());
    }

    public void setUsername(String username) {
        label_username.setText(username.toUpperCase());
    }
}
