package com.warehousemanager.ui.controllers;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import com.warehousemanager.services.AuthService;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class RegisterController implements Initializable {

    @FXML private TextField txtUsername;
    @FXML private PasswordField txtPassword;
    @FXML private PasswordField txtConfirm;
    @FXML private TextField txtEmail;
    @FXML private Label lblMessage;

    private final AuthService authService = new AuthService();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        lblMessage.setText("");
    }

    @FXML
    private void handleRegister() {
        try {
            boolean ok = authService.register(
                txtUsername.getText().trim(),
                txtPassword.getText(),
                txtConfirm.getText(),
                txtEmail.getText().trim()
            );

            if (ok) {
                showMsg("Account created successfully. Please login.", true);
                clearForm();
            } else {
                showMsg("Registration failed. Check DB connection.", false);
            }

        } catch (IllegalArgumentException e) {
            showMsg(e.getMessage(), false);
        }
    }

    @FXML
    private void goLogin() throws IOException {
        SceneSwitcher.switchScene("login.fxml");
    }

    private void clearForm() {
        txtUsername.clear();
        txtPassword.clear();
        txtConfirm.clear();
        txtEmail.clear();
    }

    private void showMsg(String msg, boolean success) {
        lblMessage.setStyle("-fx-text-fill: " + (success ? "#A6E3A1" : "#F38BA8") + ";"
            + "-fx-font-family: Georgia; -fx-font-size: 12px; -fx-font-style: italic;");
        lblMessage.setText(msg);
    }
}
