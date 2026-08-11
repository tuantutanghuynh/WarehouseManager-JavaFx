package com.warehousemanager;

import java.io.IOException;

import com.warehousemanager.ui.controllers.SceneSwitcher;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class App extends Application {

    @Override
    public void start(Stage primaryStage) throws IOException {
        SceneSwitcher.setStage(primaryStage);
        primaryStage.setTitle("Warehouse Manager");
        primaryStage.setResizable(false);

        FXMLLoader loader = new FXMLLoader(
                App.class.getResource("/com/warehousemanager/ui/views/login.fxml"));
        primaryStage.setScene(new Scene(loader.load()));
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
