module com.financemanager {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
            
                            
    opens com.financemanager to javafx.fxml;
    exports com.financemanager;
}