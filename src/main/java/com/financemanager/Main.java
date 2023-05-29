package com.financemanager;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.IOException;

public class  Main extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        // Tworzenie obiektu FXMLLoader, który pozwoli nam wczytać plik FXML
        FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("sample.fxml"));
        // Tworzenie sceny o wymiarach 600x400 wczytując plik FXML
        Scene scene = new Scene(fxmlLoader.load(), 600, 400);
        stage.setTitle("FINANCE MANAGER");
        stage.getIcons().add(new Image("file:C:/Users/Kuba/IdeaProjects/Finance Manager/src/main/java/com/financemanager/icon.png"));
        stage.setResizable(false);
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}