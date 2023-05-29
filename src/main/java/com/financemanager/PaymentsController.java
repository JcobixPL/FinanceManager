package com.financemanager;

import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.chart.PieChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.util.Pair;

import java.sql.*;
import java.sql.Date;
import java.sql.SQLException;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.UnaryOperator;

public class PaymentsController implements Initializable {

    public static String username;

    public static Integer user_id;
    public static String type;
    public static Float amount;

    public static Double oldBudgetSum;
    public static Double oldIncomeSum;
    public static Double oldExpenseSum;
    public static Double budgetSum;
    public static Double incomeSum;
    public static Double expenseSum;
    public static Integer transactionID;
    public static Double transactionAMOUNT;
    public static String transactionTYPE;
    public void setUsernameDB(String username) {
        this.username = username;
    }

    public static String transaction_type;
    public static String transaction_title;
    public static String transaction_category;
    public static LocalDate transaction_date;
    public static Float transaction_amount;

    public static DatePicker datePicker;
    public static TextField titleTextField;
    public static TextField categoryTextField;
    public static TextField amountTextField;

    @FXML
    private Button button_add_transaction;

    @FXML
    private Button button_edit_transaction;

    @FXML
    private Button button_remove_transaction;

    @FXML
    private Button button_month_filter;

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

    private TableView<Transaction> table_payments;

    @FXML
    private TableColumn<Transaction, String> typeColumn;

    @FXML
    private TableColumn<Transaction, Double> amountColumn;

    @FXML
    private TableColumn<Transaction, String> titleColumn;

    @FXML
    private TableColumn<Transaction, String> categoryColumn;

    @FXML
    private TableColumn<Transaction, LocalDate> dateColumn;

    public void setUserID(Integer user_id) {
        this.user_id = user_id;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        typeColumn.setResizable(false);
        amountColumn.setResizable(false);
        titleColumn.setResizable(false);
        categoryColumn.setResizable(false);
        dateColumn.setResizable(false);



        try {
            Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/javafx-project", "root", "Kubizon2003");
            String sqlQuery ="SELECT * FROM transactions WHERE user_id = ?";
            PreparedStatement statement = connection.prepareStatement(sqlQuery);

            // Ustaw wartości parametrów zapytania
            statement.setInt(1, user_id);

            ObservableList<Transaction> transactions = FXCollections.observableArrayList();

            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                String type = resultSet.getString("transactionType");
                double amount = resultSet.getDouble("transactionSum");
                String title = resultSet.getString("transactionName");
                String category = resultSet.getString("transactionCategory");
                LocalDate date = resultSet.getDate("transactionDate").toLocalDate();
                Integer id = resultSet.getInt("transaction_id");

                Transaction transaction = new Transaction(type, amount, title, category, date, id);
                transactions.add(transaction);
            }

            // Ustawienie danych w tabeli
            typeColumn.setCellValueFactory(new PropertyValueFactory<>("type"));
            amountColumn.setCellValueFactory(new PropertyValueFactory<>("amount"));
            titleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
            categoryColumn.setCellValueFactory(new PropertyValueFactory<>("category"));
            dateColumn.setCellValueFactory(new PropertyValueFactory<>("date"));




            table_payments.setItems(transactions);
            dateColumn.setSortType(TableColumn.SortType.DESCENDING); // Sortowanie malejące (od najnowszej)
            table_payments.getSortOrder().add(dateColumn); // Dodanie kolumny daty do sortowania

            resultSet.close();
            statement.close();
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        Thread updateTimeThread = new Thread(() -> {
            while (true) {
                LocalDateTime currentDateTime = LocalDateTime.now();
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                String formattedDateTime = currentDateTime.format(formatter);

                // Aktualizacja etykiety na wątku JavaFX
                Platform.runLater(() -> label_date_time.setText(formattedDateTime));

                try {
                    Thread.sleep(1000); // Czekaj przez 1 sekundę
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        updateTimeThread.setDaemon(true);
        updateTimeThread.start();

        button_add_transaction.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                // Tworzenie kontrolek
                RadioButton incomeRadioButton = new RadioButton("Income");
                RadioButton expenseRadioButton = new RadioButton("Expense");
                ToggleGroup toggleGroup = new ToggleGroup();
                incomeRadioButton.setToggleGroup(toggleGroup);
                expenseRadioButton.setToggleGroup(toggleGroup);
                incomeRadioButton.setSelected(true);

                TextField titleTextField = new TextField();
                TextField categoryTextField = new TextField();

                DatePicker datePicker = new DatePicker();
                datePicker.setValue(LocalDate.now());

                TextField amountTextField = new TextField();

                // Ustawianie siatki
                GridPane gridPane = new GridPane();
                gridPane.setHgap(10);
                gridPane.setVgap(10);
                gridPane.setPadding(new Insets(10));

                gridPane.add(new Label("Transaction Type:"), 0, 0);
                gridPane.add(incomeRadioButton, 1, 0);
                gridPane.add(expenseRadioButton, 2, 0);

                gridPane.add(new Label("Transaction Title:"), 0, 1);
                gridPane.add(titleTextField, 1, 1, 2, 1);

                gridPane.add(new Label("Transaction Category:"), 0, 2);
                gridPane.add(categoryTextField, 1, 2, 2, 1);

                gridPane.add(new Label("Transaction Date:"), 0, 3);
                gridPane.add(datePicker, 1, 3, 2, 1);

                gridPane.add(new Label("Transaction Amount:"), 0, 4);
                gridPane.add(amountTextField, 1, 4, 2, 1);

                // Walidacja pól i blokowanie przycisku "OK"
                ButtonType okButtonType = new ButtonType("OK", ButtonBar.ButtonData.OK_DONE);
                Dialog<ButtonType> dialog = new Dialog<>();
                dialog.setTitle("Dodawanie transakcji");
                dialog.getDialogPane().setContent(gridPane);
                dialog.getDialogPane().getButtonTypes().addAll(okButtonType, ButtonType.CANCEL);

                Node okButton = dialog.getDialogPane().lookupButton(okButtonType);
                okButton.setDisable(true);

                okButton.addEventFilter(ActionEvent.ACTION, e -> {
                    String transactionType = incomeRadioButton.isSelected() ? "Income" : "Expense";
                    String transactionTitle = titleTextField.getText();
                    String transactionCategory = categoryTextField.getText();
                    LocalDate transactionDate = datePicker.getValue();
                    float transactionAmount = Float.parseFloat(amountTextField.getText());

                    if (transactionTitle.isEmpty() || transactionCategory.isEmpty() || transactionAmount <= 0) {
                        Alert alert = new Alert(Alert.AlertType.ERROR);
                        alert.setTitle("Błąd");
                        alert.setHeaderText(null);
                        alert.setContentText("Wypełnij poprawnie wszystkie pola transakcji!");
                        alert.showAndWait();
                        e.consume();
                    } else {
                        System.out.println("Transaction Type: " + transactionType);
                        System.out.println("Transaction Title: " + transactionTitle);
                        System.out.println("Transaction Category: " + transactionCategory);
                        System.out.println("Transaction Date: " + transactionDate);
                        System.out.println("Transaction Amount: " + transactionAmount);

                        try {
                            // Utwórz połączenie do bazy danych
                            Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/javafx-project", "root", "Kubizon2003");

                            // Zapytanie SQL do wstawienia nowego wiersza
                            String sqlQuery = "INSERT INTO transactions (transactionName, transactionCategory, transactionSum, transactionDate, transactionType, user_id) " +
                                    "VALUES (?, ?, ?, ?, ?, ?)";

                            // Przygotuj zapytanie
                            PreparedStatement statement = connection.prepareStatement(sqlQuery);

                            // Ustaw wartości parametrów zapytania
                            statement.setString(1, transactionTitle);
                            statement.setString(2, transactionCategory);
                            statement.setDouble(3, transactionAmount);
                            statement.setDate(4, Date.valueOf(transactionDate));
                            statement.setString(5, transactionType);
                            statement.setInt(6, user_id);
                            type = transactionType;
                            amount = transactionAmount;
                            // Wykonaj zapytanie
                            int rowsInserted = statement.executeUpdate();

                            if (rowsInserted > 0) {
                                System.out.println("Nowy wiersz został dodany do tabeli transactions.");
                            } else {
                                System.out.println("Dodanie nowego wiersza nie powiodło się.");
                            }

                            // Zamknij połączenie i zasoby
                            statement.close();
                            connection.close();
                        } catch (SQLException ex) {
                            ex.printStackTrace();
                        }
                    }
                });

                // Blokowanie przycisku "OK" w zależności od wprowadzonych danych
                titleTextField.textProperty().addListener((observable, oldValue, newValue) -> {
                    boolean disable = newValue.isEmpty() || categoryTextField.getText().isEmpty() || amountTextField.getText().isEmpty();
                    okButton.setDisable(disable);
                });

                categoryTextField.textProperty().addListener((observable, oldValue, newValue) -> {
                    boolean disable = titleTextField.getText().isEmpty() || newValue.isEmpty() || amountTextField.getText().isEmpty();
                    ((Node) okButton).setDisable(disable);
                });

                amountTextField.textProperty().addListener((observable, oldValue, newValue) -> {
                    boolean disable = titleTextField.getText().isEmpty() || categoryTextField.getText().isEmpty() || newValue.isEmpty();
                    okButton.setDisable(disable);
                    try {
                        // Sprawdzenie, czy wprowadzony tekst może być przekonwertowany na liczbę zmiennoprzecinkową
                        Float.parseFloat(newValue.replace(',', '.'));
                        disable = disable || false; // Jeśli konwersja powiodła się, to przycisk "OK" nie jest zablokowany
                    } catch (NumberFormatException e) {
                        disable = true; // Jeśli konwersja nie powiodła się, to przycisk "OK" jest zablokowany
                    }

                    okButton.setDisable(disable);
                });

                // Utworzenie filtru dla pola amountTextField
                UnaryOperator<TextFormatter.Change> amountFilter = change -> {
                    String newText = change.getControlNewText();
                    if (newText.matches("^\\d*([,.]\\d{0,2})?$")) {
                        return change;
                    }
                    return null;
                };

                // Utworzenie TextFormattera i ustawienie filtru
                TextFormatter<String> amountTextFormatter = new TextFormatter<>(amountFilter);
                amountTextField.setTextFormatter(amountTextFormatter);


                // Wyświetlanie okna dialogowego
                dialog.showAndWait().ifPresent(result -> {
                    if (result == okButtonType) {
                        // Zatwierdzono dodanie transakcji
                    } else {
                        // Anulowano dodawanie transakcji
                    }
                });

                try {
                    Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/javafx-project", "root", "Kubizon2003");

                    String sqlQuery = "SELECT * FROM transactions WHERE user_id = ?";

                    PreparedStatement statement = connection.prepareStatement(sqlQuery);

                    // Ustaw wartości parametrów zapytania
                    statement.setInt(1, user_id);

                    ObservableList<Transaction> transactions = FXCollections.observableArrayList();

                    ResultSet resultSet = statement.executeQuery();

                    while (resultSet.next()) {
                        String type = resultSet.getString("transactionType");
                        double amount = resultSet.getDouble("transactionSum");
                        String title = resultSet.getString("transactionName");
                        String category = resultSet.getString("transactionCategory");
                        LocalDate date = resultSet.getDate("transactionDate").toLocalDate();
                        Integer id = resultSet.getInt("transaction_id");

                        Transaction transaction = new Transaction(type, amount, title, category, date, id);
                        transactions.add(transaction);
                    }

                    // Ustawienie danych w tabeli
                    typeColumn.setCellValueFactory(new PropertyValueFactory<>("type"));
                    amountColumn.setCellValueFactory(new PropertyValueFactory<>("amount"));
                    titleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
                    categoryColumn.setCellValueFactory(new PropertyValueFactory<>("category"));
                    dateColumn.setCellValueFactory(new PropertyValueFactory<>("date"));

                    table_payments.setItems(transactions);
                    dateColumn.setSortType(TableColumn.SortType.DESCENDING); // Sortowanie malejące (od najnowszej)
                    table_payments.getSortOrder().add(dateColumn); // Dodanie kolumny daty do sortowania

                    resultSet.close();
                    statement.close();
                    connection.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }

                try{
                    PreparedStatement psInsert = null;
                    Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/javafx-project", "root", "Kubizon2003");
                    Statement statement = connection.createStatement();
                    psInsert = connection.prepareStatement("SELECT budgetAmount, budgetIncome, budgetExpense FROM budgets WHERE user_id = ?");
                    psInsert.setInt(1, user_id);
                    try(ResultSet rs = psInsert.executeQuery()){
                        if(rs.next()) {
                            double budgetSum1 = rs.getDouble("budgetAmount");
                            double incomesSum1 = rs.getDouble("budgetIncome");
                            double expensesSum1 = rs.getDouble("budgetExpense");

                            if (type.equals("Income")) {
                                budgetSum1 += amount;
                                incomesSum1 += amount;
                                budgetSum = budgetSum1;
                                incomeSum = incomesSum1;
                                expenseSum = expensesSum1;
                            }
                            else if (type.equals("Expense")) {
                                budgetSum1 -= amount;
                                expensesSum1 += amount;
                                budgetSum = budgetSum1;
                                expenseSum = expensesSum1;
                                incomeSum = incomesSum1;
                            }
                            DashboardController dashboardController = new DashboardController();
                            dashboardController.getSums(incomeSum, expenseSum);
                        }
                    }

                    statement.close();
                    connection.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                    // Obsłuż błąd zapytania SQL
                }

                try {
                    Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/javafx-project", "root", "Kubizon2003");
                    String query = "UPDATE budgets SET budgetAmount = ?, budgetIncome = ?, budgetExpense = ? WHERE user_id = ?";
                    PreparedStatement stmt = connection.prepareStatement(query);
                    stmt.setDouble(1, budgetSum);
                    stmt.setDouble(2, incomeSum);
                    stmt.setDouble(3, expenseSum);
                    stmt.setInt(4, user_id);
                    int rowsAffected = stmt.executeUpdate();
                    stmt.close();
                    connection.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        });

        button_edit_transaction.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                Transaction selectedTransaction = table_payments.getSelectionModel().getSelectedItem();
                if (selectedTransaction == null) {
                    Alert alert = new Alert(Alert.AlertType.WARNING);
                    alert.setTitle("Brak wyboru");
                    alert.setHeaderText(null);
                    alert.setContentText("Wybierz transakcję, którą chcesz edytować.");
                    alert.showAndWait();
                    return;
                }
                // Tworzenie kontrolek i inicjalizacja wartościami zaznaczonej transakcji
                RadioButton incomeRadioButton = new RadioButton("Income");
                RadioButton expenseRadioButton = new RadioButton("Expense");
                ToggleGroup toggleGroup = new ToggleGroup();
                incomeRadioButton.setToggleGroup(toggleGroup);
                expenseRadioButton.setToggleGroup(toggleGroup);

                // Inicjalizacja wartościami zaznaczonej transakcji
                if (selectedTransaction.getType().equals("Income")) {
                    incomeRadioButton.setSelected(true);
                } else {
                    expenseRadioButton.setSelected(true);
                }

                TextField titleTextField = new TextField(selectedTransaction.getTitle());
                TextField categoryTextField = new TextField(selectedTransaction.getCategory());

                DatePicker datePicker = new DatePicker(selectedTransaction.getDate());

                TextField amountTextField = new TextField(String.valueOf(selectedTransaction.getAmount()));

                // Ustawianie siatki
                GridPane gridPane = new GridPane();
                gridPane.setHgap(10);
                gridPane.setVgap(10);
                gridPane.setPadding(new Insets(10));

                gridPane.add(new Label("Transaction Type:"), 0, 0);
                gridPane.add(incomeRadioButton, 1, 0);
                gridPane.add(expenseRadioButton, 2, 0);

                gridPane.add(new Label("Transaction Title:"), 0, 1);
                gridPane.add(titleTextField, 1, 1, 2, 1);

                gridPane.add(new Label("Transaction Category:"), 0, 2);
                gridPane.add(categoryTextField, 1, 2, 2, 1);

                gridPane.add(new Label("Transaction Date:"), 0, 3);
                gridPane.add(datePicker, 1, 3, 2, 1);

                gridPane.add(new Label("Transaction Amount:"), 0, 4);
                gridPane.add(amountTextField, 1, 4, 2, 1);

                // Walidacja pól i blokowanie przycisku "OK"
                ButtonType okButtonType = new ButtonType("OK", ButtonBar.ButtonData.OK_DONE);
                Dialog<ButtonType> dialog = new Dialog<>();
                dialog.setTitle("Edycja transakcji");
                dialog.getDialogPane().setContent(gridPane);
                dialog.getDialogPane().getButtonTypes().addAll(okButtonType, ButtonType.CANCEL);

                Node okButton = dialog.getDialogPane().lookupButton(okButtonType);
                okButton.setDisable(true);

                okButton.addEventFilter(ActionEvent.ACTION, e -> {
                    String transactionType = incomeRadioButton.isSelected() ? "Income" : "Expense";
                    String transactionTitle = titleTextField.getText();
                    String transactionCategory = categoryTextField.getText();
                    LocalDate transactionDate = datePicker.getValue();
                    double transactionAmount = Double.parseDouble(amountTextField.getText());

                    if (transactionTitle.isEmpty() || transactionCategory.isEmpty() || transactionAmount <= 0) {
                        Alert alert = new Alert(Alert.AlertType.ERROR);
                        alert.setTitle("Błąd");
                        alert.setHeaderText(null);
                        alert.setContentText("Wypełnij poprawnie wszystkie pola transakcji!");
                        alert.showAndWait();
                        e.consume();
                    } else {
                        System.out.println("Transaction Type: " + transactionType);
                        System.out.println("Transaction Title: " + transactionTitle);
                        System.out.println("Transaction Category: " + transactionCategory);
                        System.out.println("Transaction Date: " + transactionDate);
                        System.out.println("Transaction Amount: " + transactionAmount);

                        try {
                            // Utwórz połączenie do bazy danych
                            Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/javafx-project", "root", "Kubizon2003");

                            // Zapytanie SQL do aktualizacji danych
                            String sqlQuery = "UPDATE transactions SET transactionName = ?, transactionCategory = ?, transactionSum = ?, transactionDate = ?, transactionType = ? WHERE transaction_id = ?";

                            // Przygotuj zapytanie
                            PreparedStatement statement = connection.prepareStatement(sqlQuery);

                            // Ustaw wartości parametrów zapytania
                            statement.setString(1, transactionTitle);
                            statement.setString(2, transactionCategory);
                            statement.setDouble(3, transactionAmount);
                            statement.setDate(4, Date.valueOf(transactionDate));
                            statement.setString(5, transactionType);
                            statement.setInt(6, selectedTransaction.getId()); // Ustawienie ID zaznaczonej transakcji

                            // Wykonaj zapytanie
                            int rowsUpdated = statement.executeUpdate();

                            if (rowsUpdated > 0) {
                                System.out.println("Wiersz w tabeli transactions został zaktualizowany.");
                            } else {
                                System.out.println("Aktualizacja wiersza nie powiodła się.");
                            }

                            // Zamknij połączenie i zasoby
                            statement.close();
                            connection.close();
                        } catch (SQLException ex) {
                            ex.printStackTrace();
                        }

                        try{
                            PreparedStatement psInsert = null;
                            Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/javafx-project", "root", "Kubizon2003");
                            Statement statement = connection.createStatement();
                            psInsert = connection.prepareStatement("SELECT budgetAmount, budgetIncome, budgetExpense FROM budgets WHERE user_id = ?");
                            psInsert.setInt(1, user_id);
                            try(ResultSet rs = psInsert.executeQuery()){
                                if(rs.next()) {
                                    double budgetSum1 = rs.getDouble("budgetAmount");
                                    double incomesSum1 = rs.getDouble("budgetIncome");
                                    double expensesSum1 = rs.getDouble("budgetExpense");

                                    oldBudgetSum = budgetSum1;
                                    oldIncomeSum = incomesSum1;
                                    oldExpenseSum = expensesSum1;

                                    if(selectedTransaction.getType().equals("Income")) {
                                        oldBudgetSum -= selectedTransaction.getAmount();
                                        oldIncomeSum -= selectedTransaction.getAmount();
                                    }
                                    else if (selectedTransaction.getType().equals("Expense")) {
                                        oldBudgetSum += selectedTransaction.getAmount();
                                        oldExpenseSum -= selectedTransaction.getAmount();
                                    }
                                    if(transactionType.equals("Income")) {
                                        oldBudgetSum += transactionAmount;
                                        oldIncomeSum += transactionAmount;
                                    }
                                    else if(transactionType.equals("Expense")) {
                                        oldBudgetSum -= transactionAmount;
                                        oldExpenseSum += transactionAmount;
                                    }

                                    DashboardController dashboardController = new DashboardController();
                                    dashboardController.getSums(oldIncomeSum, oldExpenseSum);
                                }

                            }

                            statement.close();
                            connection.close();
                        } catch (SQLException e1) {
                            e1.printStackTrace();
                            // Obsłuż błąd zapytania SQL
                        }






                        System.out.println(oldBudgetSum);
                        System.out.println(oldIncomeSum);
                        System.out.println(oldExpenseSum);
                        System.out.println(user_id);

                        try{
                            Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/javafx-project", "root", "Kubizon2003");
                            String sqlQuery = "UPDATE budgets SET budgetAmount = ?, budgetIncome = ?, budgetExpense = ? WHERE user_id = ?";
                            PreparedStatement statement = connection.prepareStatement(sqlQuery);
                            statement.setDouble(1, oldBudgetSum);
                            statement.setDouble(2, oldIncomeSum);
                            statement.setDouble(3, oldExpenseSum);
                            statement.setInt(4, user_id);
                            int rowsAffected = statement.executeUpdate();
                            statement.close();
                            connection.close();
                        }
                        catch (SQLException e1) {
                            e1.printStackTrace();
                        }



                    }
                });

                // Blokowanie przycisku "OK" w zależności od wprowadzonych danych
                titleTextField.textProperty().addListener((observable, oldValue, newValue) -> {
                    boolean disable = newValue.isEmpty() || categoryTextField.getText().isEmpty() || amountTextField.getText().isEmpty();
                    okButton.setDisable(disable);
                });

                categoryTextField.textProperty().addListener((observable, oldValue, newValue) -> {
                    boolean disable = titleTextField.getText().isEmpty() || newValue.isEmpty() || amountTextField.getText().isEmpty();
                    okButton.setDisable(disable);
                });

                amountTextField.textProperty().addListener((observable, oldValue, newValue) -> {
                    boolean disable = titleTextField.getText().isEmpty() || categoryTextField.getText().isEmpty() || newValue.isEmpty();
                    okButton.setDisable(disable);
                    try {
                        // Sprawdzenie, czy wprowadzony tekst może być przekonwertowany na liczbę zmiennoprzecinkową
                        Double.parseDouble(newValue);
                        disable = disable || false; // Jeśli konwersja powiodła się, to przycisk "OK" nie jest zablokowany
                    } catch (NumberFormatException e) {
                        disable = true; // Jeśli konwersja nie powiodła się, to przycisk "OK" jest zablokowany
                    }

                    okButton.setDisable(disable);
                });

                // Utworzenie filtru dla pola amountTextField
                UnaryOperator<TextFormatter.Change> amountFilter = change -> {
                    String newText = change.getControlNewText();
                    if (newText.matches("^\\d*([,.]\\d{0,2})?$")) {
                        return change;
                    }
                    return null;
                };

                // Utworzenie TextFormattera i ustawienie filtru
                TextFormatter<String> amountTextFormatter = new TextFormatter<>(amountFilter);
                amountTextField.setTextFormatter(amountTextFormatter);

                // Wyświetlanie dialogu
                dialog.showAndWait();
                try {
                    Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/javafx-project", "root", "Kubizon2003");

                    String sqlQuery = "SELECT * FROM transactions WHERE user_id = ?";

                    PreparedStatement statement = connection.prepareStatement(sqlQuery);

                    // Ustaw wartości parametrów zapytania
                    statement.setInt(1, user_id);

                    ObservableList<Transaction> transactions = FXCollections.observableArrayList();

                    ResultSet resultSet = statement.executeQuery();

                    while (resultSet.next()) {
                        String type = resultSet.getString("transactionType");
                        double amount = resultSet.getDouble("transactionSum");
                        String title = resultSet.getString("transactionName");
                        String category = resultSet.getString("transactionCategory");
                        LocalDate date = resultSet.getDate("transactionDate").toLocalDate();
                        Integer id = resultSet.getInt("transaction_id");

                        Transaction transaction = new Transaction(type, amount, title, category, date, id);
                        transactions.add(transaction);
                    }

                    // Ustawienie danych w tabeli
                    typeColumn.setCellValueFactory(new PropertyValueFactory<>("type"));
                    amountColumn.setCellValueFactory(new PropertyValueFactory<>("amount"));
                    titleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
                    categoryColumn.setCellValueFactory(new PropertyValueFactory<>("category"));
                    dateColumn.setCellValueFactory(new PropertyValueFactory<>("date"));

                    table_payments.setItems(transactions);
                    dateColumn.setSortType(TableColumn.SortType.DESCENDING); // Sortowanie malejące (od najnowszej)
                    table_payments.getSortOrder().add(dateColumn); // Dodanie kolumny daty do sortowania

                    resultSet.close();
                    statement.close();
                    connection.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }


            }
        });

        button_remove_transaction.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                Transaction selectedTransaction = table_payments.getSelectionModel().getSelectedItem();
                if (selectedTransaction == null) {
                    Alert alert = new Alert(Alert.AlertType.WARNING);
                    alert.setTitle("Brak wyboru");
                    alert.setHeaderText(null);
                    alert.setContentText("Wybierz transakcję, którą chcesz usunąć.");
                    alert.showAndWait();
                    return;
                }

                Alert confirmationDialog = new Alert(Alert.AlertType.CONFIRMATION);
                confirmationDialog.setTitle("Potwierdzenie");
                confirmationDialog.setHeaderText(null);
                confirmationDialog.setContentText("Czy na pewno chcesz usunąć zaznaczoną transakcję?");

                Optional<ButtonType> result = confirmationDialog.showAndWait();
                if (result.isPresent() && result.get() == ButtonType.OK) {

                    try {
                        Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/javafx-project", "root", "Kubizon2003");
                        String sqlQuery = "SELECT transaction_id, transactionSum, transactionType FROM transactions WHERE user_id = ? AND transaction_id = ?";
                        PreparedStatement statement = connection.prepareStatement(sqlQuery);

                        statement.setInt(1, user_id);
                        statement.setInt(2, selectedTransaction.getId());
                        ResultSet resultSet = statement.executeQuery();

                        if(resultSet.next()) {
                            Integer transaction_id = resultSet.getInt("transaction_id");
                            double transactionAmount = resultSet.getDouble("transactionSum");
                            String transactionType = resultSet.getString("transactionType");

                            transactionID = transaction_id;
                            transactionAMOUNT = transactionAmount;
                            transactionTYPE = transactionType;
                        }


                    } catch (SQLException e1) {
                        e1.printStackTrace();
                    }


                    try {
                        Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/javafx-project", "root", "Kubizon2003");

                        String sqlQuery = "DELETE FROM transactions WHERE transaction_id = ?";
                        PreparedStatement statement = connection.prepareStatement(sqlQuery);
                        statement.setInt(1, selectedTransaction.getId());

                        int rowsDeleted = statement.executeUpdate();
                        if (rowsDeleted > 0) {
                            System.out.println("Transakcja została usunięta.");
                        } else {
                            System.out.println("Usuwanie transakcji nie powiodło się.");
                        }

                        statement.close();
                        connection.close();
                    } catch (SQLException ex) {
                        ex.printStackTrace();
                    }

                    ///start work
                    try {
                        PreparedStatement psInsert = null;
                        Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/javafx-project", "root", "Kubizon2003");
                        Statement statement = connection.createStatement();
                        psInsert = connection.prepareStatement("SELECT budgetAmount, budgetIncome, budgetExpense FROM budgets WHERE user_id = ?");
                        psInsert.setInt(1, user_id);

                        try(ResultSet resultSet = psInsert.executeQuery()){
                            if(resultSet.next()) {
                                oldBudgetSum = resultSet.getDouble("budgetAmount");
                                oldIncomeSum = resultSet.getDouble("budgetIncome");
                                oldExpenseSum = resultSet.getDouble("budgetExpense");
                            }

                            if(transactionTYPE.equals("Income")) {
                                oldBudgetSum -= transactionAMOUNT;
                                oldIncomeSum -= transactionAMOUNT;
                            }
                            else if(transactionTYPE.equals("Expense")) {
                                oldBudgetSum += transactionAMOUNT;
                                oldExpenseSum -= transactionAMOUNT;
                            }
                            DashboardController dashboardController = new DashboardController();
                            dashboardController.getSums(oldIncomeSum, oldExpenseSum);

                        }

                        statement.close();
                        connection.close();
                    } catch (SQLException e1) {
                        e1.printStackTrace();
                    }

                    try {
                        Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/javafx-project", "root", "Kubizon2003");
                        String sqlQuery = "UPDATE budgets SET budgetAmount = ?, budgetIncome = ?, budgetExpense = ? WHERE user_id = ?";
                        PreparedStatement statement = connection.prepareStatement(sqlQuery);
                        statement.setDouble(1, oldBudgetSum);
                        statement.setDouble(2, oldIncomeSum);
                        statement.setDouble(3, oldExpenseSum);
                        statement.setInt(4, user_id);
                        int rowsAffected = statement.executeUpdate();
                        statement.close();
                        connection.close();

                    } catch (SQLException e1) {
                        e1.printStackTrace();
                    }

                    ///end work
                }

                // Odświeżanie danych w tabeli
                try {
                    Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/javafx-project", "root", "Kubizon2003");
                    Statement statement = connection.createStatement();
                    ResultSet resultSet = statement.executeQuery("SELECT * FROM transactions");

                    ObservableList<Transaction> transactions = FXCollections.observableArrayList();

                    while (resultSet.next()) {
                        String type = resultSet.getString("transactionType");
                        double amount = resultSet.getDouble("transactionSum");
                        String title = resultSet.getString("transactionName");
                        String category = resultSet.getString("transactionCategory");
                        LocalDate date = resultSet.getDate("transactionDate").toLocalDate();
                        Integer id = resultSet.getInt("transaction_id");

                        Transaction transaction = new Transaction(type, amount, title, category, date, id);
                        transactions.add(transaction);
                    }

                    typeColumn.setCellValueFactory(new PropertyValueFactory<>("type"));
                    amountColumn.setCellValueFactory(new PropertyValueFactory<>("amount"));
                    titleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
                    categoryColumn.setCellValueFactory(new PropertyValueFactory<>("category"));
                    dateColumn.setCellValueFactory(new PropertyValueFactory<>("date"));

                    table_payments.setItems(transactions);
                    dateColumn.setSortType(TableColumn.SortType.DESCENDING);
                    table_payments.getSortOrder().add(dateColumn);

                    resultSet.close();
                    statement.close();
                    connection.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        });

        button_month_filter.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                Dialog<String> dialog = new Dialog<>();
                dialog.setTitle("Filtrowanie transakcji");
                dialog.setHeaderText(null);
                dialog.setContentText("Wybierz opcję:");

                ButtonType showAllButtonType = new ButtonType("SHOW ALL", ButtonBar.ButtonData.OK_DONE);
                ButtonType selectMonthFilterButtonType = new ButtonType("SELECT MONTH FILTER", ButtonBar.ButtonData.OK_DONE);
                dialog.getDialogPane().getButtonTypes().addAll(showAllButtonType, selectMonthFilterButtonType, ButtonType.CANCEL);



                dialog.setResultConverter(dialogButton -> {
                    if (dialogButton == showAllButtonType) {
                        return "SHOW_ALL";
                    } else if (dialogButton == selectMonthFilterButtonType) {
                        return "SELECT_MONTH_FILTER";
                    }
                    return null;
                });

                Optional<String> result = dialog.showAndWait();
                if (result.isPresent()) {
                    if (result.get().equals("SHOW_ALL")) {
                        // Wyświetlanie wszystkich transakcji
                        try {
                            Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/javafx-project", "root", "Kubizon2003");
                            Statement statement = connection.createStatement();
                            ResultSet resultSet = statement.executeQuery("SELECT * FROM transactions");

                            ObservableList<Transaction> transactions = FXCollections.observableArrayList();

                            while (resultSet.next()) {
                                String type = resultSet.getString("transactionType");
                                double amount = resultSet.getDouble("transactionSum");
                                String title = resultSet.getString("transactionName");
                                String category = resultSet.getString("transactionCategory");
                                LocalDate date = resultSet.getDate("transactionDate").toLocalDate();
                                Integer id = resultSet.getInt("transaction_id");

                                Transaction transaction = new Transaction(type, amount, title, category, date, id);
                                transactions.add(transaction);
                            }

                            typeColumn.setCellValueFactory(new PropertyValueFactory<>("type"));
                            amountColumn.setCellValueFactory(new PropertyValueFactory<>("amount"));
                            titleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
                            categoryColumn.setCellValueFactory(new PropertyValueFactory<>("category"));
                            dateColumn.setCellValueFactory(new PropertyValueFactory<>("date"));

                            table_payments.setItems(transactions);
                            dateColumn.setSortType(TableColumn.SortType.DESCENDING);
                            table_payments.getSortOrder().add(dateColumn);

                            resultSet.close();
                            statement.close();
                            connection.close();
                        } catch (SQLException e) {
                            e.printStackTrace();
                        }
                    } else if (result.get().equals("SELECT_MONTH_FILTER")) {
                        // Wybór miesiąca i roku do filtrowania
                        Dialog<YearMonth> monthFilterDialog = new Dialog<>();
                        monthFilterDialog.setTitle("Wybór miesiąca i roku");
                        monthFilterDialog.setHeaderText(null);
                        monthFilterDialog.setContentText("Wybierz miesiąc i rok:");

                        ObservableList<Month> months = FXCollections.observableArrayList(Month.values());
                        ComboBox<Month> monthComboBox = new ComboBox<>(months);

                        // Pobranie listy lat z bazy danych
                        Set<Integer> yearsSet = new HashSet<>();
                        try {
                            Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/javafx-project", "root", "Kubizon2003");
                            Statement statement = connection.createStatement();
                            ResultSet resultSet = statement.executeQuery("SELECT DISTINCT YEAR(transactionDate) FROM transactions");

                            while (resultSet.next()) {
                                int year = resultSet.getInt(1);
                                yearsSet.add(year);
                            }

                            resultSet.close();
                            statement.close();
                            connection.close();
                        } catch (SQLException e) {
                            e.printStackTrace();
                        }

                        ObservableList<Integer> years = FXCollections.observableArrayList(yearsSet);
                        ComboBox<Integer> yearComboBox = new ComboBox<>(years);


                        GridPane gridPane = new GridPane();
                        gridPane.setHgap(10);
                        gridPane.setVgap(10);
                        gridPane.setPadding(new Insets(10));

                        gridPane.add(new Label("Miesiąc:"), 0, 0);
                        gridPane.add(monthComboBox, 1, 0);
                        gridPane.add(new Label("Rok:"), 0, 1);
                        gridPane.add(yearComboBox, 1, 1);

                        monthFilterDialog.getDialogPane().setContent(gridPane);
                        monthFilterDialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

                        monthFilterDialog.setResultConverter(dialogButton -> {
                            if (dialogButton == ButtonType.OK) {
                                Month selectedMonth = monthComboBox.getValue();
                                Integer selectedYear = yearComboBox.getValue();

                                if (selectedMonth != null && selectedYear != null) {
                                    return YearMonth.of(selectedYear, selectedMonth);
                                }
                            }
                            return null;
                        });

                        Optional<YearMonth> monthResult = monthFilterDialog.showAndWait();
                        if (monthResult.isPresent()) {
                            YearMonth selectedYearMonth = monthResult.get();

                            // Filtrowanie transakcji wg wybranego miesiąca i roku
                            try {
                                Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/javafx-project", "root", "Kubizon2003");
                                String sqlQuery = "SELECT * FROM transactions WHERE YEAR(transactionDate) = ? AND MONTH(transactionDate) = ?";
                                PreparedStatement statement = connection.prepareStatement(sqlQuery);
                                statement.setInt(1, selectedYearMonth.getYear());
                                statement.setInt(2, selectedYearMonth.getMonthValue());

                                ResultSet resultSet = statement.executeQuery();

                                ObservableList<Transaction> transactions = FXCollections.observableArrayList();

                                while (resultSet.next()) {
                                    String type = resultSet.getString("transactionType");
                                    double amount = resultSet.getDouble("transactionSum");
                                    String title = resultSet.getString("transactionName");
                                    String category = resultSet.getString("transactionCategory");
                                    LocalDate date = resultSet.getDate("transactionDate").toLocalDate();
                                    Integer id = resultSet.getInt("transaction_id");

                                    Transaction transaction = new Transaction(type, amount, title, category, date, id);
                                    transactions.add(transaction);
                                }

                                typeColumn.setCellValueFactory(new PropertyValueFactory<>("type"));
                                amountColumn.setCellValueFactory(new PropertyValueFactory<>("amount"));
                                titleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
                                categoryColumn.setCellValueFactory(new PropertyValueFactory<>("category"));
                                dateColumn.setCellValueFactory(new PropertyValueFactory<>("date"));

                                table_payments.setItems(transactions);
                                dateColumn.setSortType(TableColumn.SortType.DESCENDING);
                                table_payments.getSortOrder().add(dateColumn);

                                resultSet.close();
                                statement.close();
                                connection.close();
                            } catch (SQLException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
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
    }



    public void setUserInformation(String username) {
        label_user.setText(username.toUpperCase());
    }
}

