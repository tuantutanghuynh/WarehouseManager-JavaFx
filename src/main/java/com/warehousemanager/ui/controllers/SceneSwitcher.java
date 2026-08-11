package com.warehousemanager.ui.controllers;

import java.io.IOException;

import com.warehousemanager.App;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class SceneSwitcher {

    private static Stage stage;

    public static void setStage(Stage s) {
        stage = s;
    }

    public static void switchScene(String fxml) throws IOException {
        FXMLLoader loader = new FXMLLoader(
                App.class.getResource("/com/warehousemanager/ui/views/" + fxml));
        stage.setScene(new Scene(loader.load()));
    }

    public static <C> C switchSceneAndGetController(String fxml) throws IOException {
        FXMLLoader loader = new FXMLLoader(
                App.class.getResource("/com/warehousemanager/ui/views/" + fxml));
        stage.setScene(new Scene(loader.load()));
        return loader.getController();
    }
}