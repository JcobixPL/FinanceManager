package com.financemanager;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.*;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;

import javafx.scene.layout.AnchorPane;


import javafx.scene.chart.PieChart;
import javafx.scene.paint.Color;


import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.format.DateTimeFormatter;
import java.time.YearMonth;
import java.net.URL;
import java.time.format.TextStyle;
import java.util.*;

import static com.financemanager.PaymentsController.*;


public class DashboardController implements Initializable {

    public static String username;

    public static Integer user_id;
    public void setUsernameDB(String username) {
        this.username = username;
    }
    public void setUserID(Integer user_id) {
        this.user_id = user_id;
    }

    public static double incomes;
    public static double expenses;

    public static double incomeSum;
    public static double expenseSum;
    public static double budgetSum;
    public static double budgetStarter;

    public void getSums(double income, double expense) {
        this.incomes = income;
        this.expenses = expense;
    }

    @FXML
    private Label label_current_budget_sum;

    @FXML
    private Label label_incomes_balance_sum;

    @FXML
    private Label label_expenses_balance_sum;

    @FXML
    private Label label_transaction_balance_sum;

    @FXML
    private Label label_user; //pole representing label with username on main page

    @FXML
    private Button button_logout; //pole representing menu logout button

    @FXML
    private Button button_dashboard; //pole representing menu dashboard button

    @FXML
    private Button button_payments; //pole representing menu payments button

    @FXML
    private Button button_account; //pole representing menu account button

    @FXML
    private Label label_date_time; //pole representing actual date and time refreshing in 1 second

    @FXML
    private Label label_date;

    @FXML
    private PieChart piechart_balance;


    public void setCurrentDate() {
        LocalDate currentDate = LocalDate.now();
        String monthName = currentDate.getMonth().getDisplayName(TextStyle.FULL, Locale.ENGLISH);
        String formattedDate = monthName.toUpperCase() + " " + currentDate.getYear();
        label_date.setText(formattedDate);
    }

    // Metoda do pobierania aktualnego budżetu z bazy danych
    public void updateCurrentBudgetLabel() {
        LocalDate currentDate = LocalDate.now();
        try (Connection connection1 = DriverManager.getConnection("jdbc:mysql://localhost:3306/javafx-project", "root", "Kubizon2003")) {
            String query1 = "SELECT SUM(transactionSum) FROM transactions WHERE user_id = ? AND transactionType = ? AND MONTH(transactionDate) = ?";

            try{
                PreparedStatement statement = connection1.prepareStatement(query1);
                statement.setInt(1, user_id);
                statement.setString(2, "Income");
                statement.setInt(3, currentDate.getMonthValue());
                try(ResultSet resultSet = statement.executeQuery()){

                    if(resultSet.next()) {
                        incomeSum = resultSet.getDouble(1);
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            try{
                PreparedStatement statement = connection1.prepareStatement(query1);
                statement.setInt(1, user_id);
                statement.setString(2, "Expense");
                statement.setInt(3, currentDate.getMonthValue());
                try(ResultSet resultSet = statement.executeQuery()){

                    if(resultSet.next()) {
                        expenseSum = resultSet.getDouble(1);
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            budgetSum = 0.0;
            String query2 = "SELECT SUM(transactionSum) FROM transactions WHERE user_id = ? AND transactionType = ?";
            try{
                PreparedStatement statement = connection1.prepareStatement(query2);
                statement.setInt(1, user_id);
                statement.setString(2, "Income");
                try(ResultSet resultSet = statement.executeQuery()){

                    if(resultSet.next()) {
                         budgetSum += resultSet.getDouble(1);
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            try{
                PreparedStatement statement = connection1.prepareStatement(query2);
                statement.setInt(1, user_id);
                statement.setString(2, "Expense");
                try(ResultSet resultSet = statement.executeQuery()){
                    if(resultSet.next()) {
                        budgetSum -= resultSet.getDouble(1);
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }

            try{
                Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/javafx-project", "root", "Kubizon2003");
                String query = "SELECT budgetStarter FROM budgets WHERE user_id = ?";
                PreparedStatement statement = connection.prepareStatement(query);
                statement.setInt(1, user_id);
                try(ResultSet resultSet = statement.executeQuery()){

                    if(resultSet.next()) {
                        budgetStarter = resultSet.getDouble(1);
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
                statement.close();
                connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            System.out.println(budgetSum);
            System.out.println(budgetStarter);
            System.out.println(incomeSum);
            System.out.println(expenseSum);
            budgetSum = budgetSum + budgetStarter;
            label_current_budget_sum.setText(String.valueOf(String.format("%.2f", budgetSum)) + " zł");
            label_incomes_balance_sum.setText(String.valueOf(String.format("%.2f", incomeSum)) + " zł");
            label_expenses_balance_sum.setText(String.valueOf(String.format("%.2f", expenseSum)) + " zł");
            label_transaction_balance_sum.setText(String.valueOf(String.format("%.2f", incomeSum - expenseSum)) + " zł");

            updatePieChart(incomeSum, expenseSum);

        } catch (SQLException e) {
            e.printStackTrace();
            // Obsłuż błąd zapytania SQL
        }
    }

    @FXML
    private LineChart<String, Number> linechart_balance;

    @FXML
    private void updatePieChart(double incomeSum, double expenseSum) {
        double incomePercentage = (incomeSum / (incomeSum + expenseSum)) * 100;
        double expensePercentage = (expenseSum / (incomeSum + expenseSum)) * 100;

        PieChart.Data incomeData = new PieChart.Data("Incomes (" + String.format("%.2f", incomePercentage) + "%)", incomeSum);
        PieChart.Data expenseData = new PieChart.Data("Expenses (" + String.format("%.2f", expensePercentage) + "%)", expenseSum);


        piechart_balance.getData().setAll(incomeData, expenseData);
    }

    private void updateLineChart() {
        List<String> months = getLast6Months();
        List<Double> incomeSums = getTransactionSumsByType("Income");
        List<Double> expenseSums = getTransactionSumsByType("Expense");

        // Utworzenie serii danych dla przychodów
        XYChart.Series<String, Number> incomeSeries = new XYChart.Series<>();
        incomeSeries.setName("Incomes");
        for (int i = 0; i < months.size(); i++) {
            incomeSeries.getData().add(new XYChart.Data<>(months.get(i), incomeSums.get(i)));
        }

        // Utworzenie serii danych dla wydatków
        XYChart.Series<String, Number> expenseSeries = new XYChart.Series<>();
        expenseSeries.setName("Expenses");
        for (int i = 0; i < months.size(); i++) {
            expenseSeries.getData().add(new XYChart.Data<>(months.get(i), expenseSums.get(i)));
        }

        // Usunięcie istniejących danych z wykresu
        linechart_balance.getData().clear();

        // Dodanie nowych danych do wykresu
        linechart_balance.getData().addAll(incomeSeries, expenseSeries);


    }



    private List<String> getLast6Months() {
        List<String> months = new ArrayList<>();
        YearMonth currentMonth = YearMonth.now();
        DateTimeFormatter monthYearFormatter = DateTimeFormatter.ofPattern("yyyy-MM");

        for (int i = 5; i >= 0; i--) {
            YearMonth month = currentMonth.minusMonths(i);
            String formattedMonth = month.format(monthYearFormatter);
            months.add(formattedMonth);
        }

        return months;
    }

    private List<Double> getTransactionSumsByType(String transactionType) {
        List<Double> sums = new ArrayList<>();

        try (Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/javafx-project", "root", "Kubizon2003")) {
            for (String month : getLast6Months()) {
                double sum = 0.0;

                String query = "SELECT SUM(transactionSum) FROM transactions WHERE transactionType = ? AND transactionDate LIKE ? AND user_id = ?";
                PreparedStatement statement = connection.prepareStatement(query);
                statement.setString(1, transactionType);
                statement.setString(2, month + "%");
                statement.setInt(3, user_id);

                ResultSet resultSet = statement.executeQuery();
                if (resultSet.next()) {
                    sum = resultSet.getDouble(1);
                }

                sums.add(sum);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            // Obsługa błędu połączenia z bazą danych
        }

        return sums;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        updateLineChart();
        updateCurrentBudgetLabel();
        setCurrentDate();

        // Aktualizacja daty i czasu co sekundę
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
    }



    public void setUserInformation(String username) {
        label_user.setText(username.toUpperCase());
    }
    // Metoda do pobierania aktualnego budżetu z bazy danych



}
