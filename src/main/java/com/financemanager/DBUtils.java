package com.financemanager;

import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Stage;
import java.io.IOException;
import java.sql.*;

public class DBUtils {
    //pola przechowujące informacje o zalogowanym użytkowniku
    public static String user;
    public static Integer user_id;
    //metoda zmieniająca scene
    public static void changeSceneDash(ActionEvent event, String fxmlFile, String username) {
        Parent root = null; //Parent root jako rodzic sceny
        try {
            //Tworzenie obiektu FXMLLoader do wczytywania plikow FXML i wczytywanie go do zmiennej root
            FXMLLoader loader = new FXMLLoader(DBUtils.class.getResource(fxmlFile));
            root = loader.load();
            if (username != null) {
                if (fxmlFile.equals("dashboard.fxml")) {
                    DashboardController dashboardController = loader.getController();
                    dashboardController.setUserInformation(username);
                }
                else if (fxmlFile.equals("payments.fxml")) {
                    PaymentsController paymentsController = loader.getController();
                    paymentsController.setUserInformation(username);
                }
                else if (fxmlFile.equals("account.fxml")) {
                    AccountController accountController = loader.getController();
                    accountController.setUsername(username);
                    accountController.setUserInformation(username);
                    accountController.setUsernameDB(username);
                }
            }
        }catch (IOException e) {
            e.printStackTrace();
        }
        // Pobranie referencji do aktualnego okna
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        if (fxmlFile.equals("sample.fxml") || fxmlFile.equals("sign-up.fxml")) {
            stage.setScene(new Scene(root, 600, 400));
        } else {
            stage.setScene(new Scene(root, 1022, 688));
        }
        stage.show();
    }

    public static void signUpUser(ActionEvent event, String username, String password, double budget) {
        Connection connection = null;
        PreparedStatement psInsert = null;
        PreparedStatement psCheckUserExsist = null;
        ResultSet resultSet = null;
        try {

            connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/javafx-project", "root", "Kubizon2003");
            psCheckUserExsist = connection.prepareStatement("SELECT * FROM users WHERE username = ?");
            psCheckUserExsist.setString(1, username);
            resultSet = psCheckUserExsist.executeQuery();

            if(resultSet.isBeforeFirst()) {
                System.out.println("user already exists");
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setContentText("You cannot use this username");
                alert.show();
            }
            else {
                connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/javafx-project", "root", "Kubizon2003");
                psInsert = connection.prepareStatement("INSERT INTO users (username, password) VALUES (?, ?)");
                psInsert.setString(1, username);
                psInsert.setString(2, password);
                psInsert.executeUpdate();

                DashboardController dashboardController = new DashboardController();
                dashboardController.setUsernameDB(username);

                PaymentsController paymentsController = new PaymentsController();
                paymentsController.setUsernameDB(username);
                AccountController accountController = new AccountController();
                accountController.setUsernameDB(username);
                accountController.setPasswordDB(password);
                connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/javafx-project", "root", "Kubizon2003");
                psInsert = connection.prepareStatement("SELECT user_id FROM users WHERE username = ?");
                psInsert.setString(1, username);
                System.out.println(username);
                try (ResultSet rs = psInsert.executeQuery()) {
                    if (rs.next()) {
                        user_id = rs.getInt("user_id");
                    }
                }
                dashboardController.setUserID(user_id);
                paymentsController.setUserID(user_id);

                try {
                    connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/javafx-project", "root", "Kubizon2003");
                    psInsert = connection.prepareStatement("INSERT INTO budgets (user_id, budgetAmount, budgetIncome, budgetExpense, budgetStarter) VALUES (?, ?, ?, ?, ?)");
                    double initialBudgetAmount = budget;
                    double initialBudgetIncome = 0.0;
                    double initialBudgetExpense = 0.0;
                    psInsert.setInt(1, user_id);
                    psInsert.setDouble(2, initialBudgetAmount);
                    psInsert.setDouble(3, initialBudgetIncome);
                    psInsert.setDouble(4, initialBudgetExpense);
                    psInsert.setDouble(5, initialBudgetAmount);
                    int rowsInserted = psInsert.executeUpdate();
                    psInsert.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                    // Obsłuż błąd zapytania SQL
                }

                DashboardController dashboardController1 = new DashboardController();
                dashboardController1.getSums(0,0);

                changeSceneDash(event, "dashboard.fxml", username);
            }
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
        finally {
            if (resultSet != null) {
                try {
                    resultSet.close();
                }
                catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            if (psCheckUserExsist != null) {
                try {
                    psCheckUserExsist.close();
                }
                catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            if (psInsert != null) {
                try {
                    psInsert.close();
                }
                catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            if (connection != null) {
                try {
                    connection.close();
                }
                catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static void logInUser(ActionEvent event, String username, String password) {
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        try {
            connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/javafx-project", "root", "Kubizon2003");
            preparedStatement = connection.prepareStatement("SELECT password, user_id FROM users WHERE username = ?");
            preparedStatement.setString(1, username);

            resultSet = preparedStatement.executeQuery();
            user = username;

            if(!resultSet.isBeforeFirst()) {
                System.out.println("User not found");
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setContentText("Provided credentials are incorrect");
                alert.show();
            }
            else {
                while(resultSet.next()) {
                    String retrievedPassword = resultSet.getString("password");
                    Integer retrievedUserId = resultSet.getInt("user_id");

                    if(retrievedPassword.equals(password)) {
                        DashboardController dashboardController = new DashboardController();
                        dashboardController.setUsernameDB(username);
                        PaymentsController paymentsController = new PaymentsController();
                        paymentsController.setUsernameDB(username);
                        AccountController accountController = new AccountController();
                        accountController.setUsernameDB(username);
                        accountController.setPasswordDB(password);
                        accountController.getUserId(retrievedUserId);

                        connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/javafx-project", "root", "Kubizon2003");
                        preparedStatement = connection.prepareStatement("SELECT user_id FROM users WHERE username = ?");
                        preparedStatement.setString(1, username);
                        System.out.println(username);
                        try (ResultSet rs = preparedStatement.executeQuery()) {
                            if (rs.next()) {
                                user_id = rs.getInt("user_id");
                            }
                        }

                        dashboardController.setUserID(user_id);
                        paymentsController.setUserID(user_id);

                        try{
                            PreparedStatement psInsert = null;
                            Connection connection1 = DriverManager.getConnection("jdbc:mysql://localhost:3306/javafx-project", "root", "Kubizon2003");
                            Statement statement = connection1.createStatement();
                            psInsert = connection1.prepareStatement("SELECT budgetIncome, budgetExpense FROM budgets WHERE user_id = ?");
                            psInsert.setInt(1, user_id);
                            try(ResultSet rs = psInsert.executeQuery()){
                                if(rs.next()) {
                                    double incomesSum1 = rs.getDouble("budgetIncome");
                                    double expensesSum1 = rs.getDouble("budgetExpense");

                                    DashboardController dashboardController1 = new DashboardController();
                                    dashboardController1.getSums(incomesSum1, expensesSum1);
                                }
                            }
                            statement.close();
                            connection1.close();
                        } catch (SQLException e) {
                            e.printStackTrace();
                            // Obsłuż błąd zapytania SQL
                        }

                        changeSceneDash(event, "dashboard.fxml", username);
                    }
                    else {
                        System.out.println("Password is not correct");
                        Alert alert = new Alert(Alert.AlertType.ERROR);
                        alert.setContentText("Provided credentials are incorrect");
                        alert.show();
                    }
                }
            }
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
        finally {
            if(resultSet != null) {
                try {
                    resultSet.close();
                }
                catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            if(preparedStatement != null) {
                try {
                    preparedStatement.close();
                }
                catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            if(connection != null) {
                try {
                    connection.close();
                }
                catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }



    }

}
