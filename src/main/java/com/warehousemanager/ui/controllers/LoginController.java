package com.warehousemanager.ui.controllers;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import com.warehousemanager.models.dto.LoginRequest;
import com.warehousemanager.models.entity.User;
import com.warehousemanager.services.AuthService;
import com.warehousemanager.session.UserSession;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class LoginController implements Initializable {

    @FXML private TextField txtUsername;
    @FXML private PasswordField txtPassword;
    @FXML private Label lblMessage;

    private final AuthService authService = new AuthService();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        lblMessage.setText("");
    }

    @FXML
    private void handleLogin() throws IOException {
        try {
            User u = authService.login(new LoginRequest(
                txtUsername.getText().trim(),
                txtPassword.getText()
            ));

            if (u == null) {
                showMsg("Invalid username or password.", false);
                return;
            }
            if (!u.isStatus()) {
                showMsg("Account is locked. Contact admin.", false);
                return;
            }

            UserSession.set(u);
            SceneSwitcher.switchScene("dashboard.fxml");

        } catch (IllegalArgumentException e) {
            showMsg(e.getMessage(), false);
        }
    }

    @FXML
    private void goRegister() throws IOException {
        SceneSwitcher.switchScene("register.fxml");
    }

    private void showMsg(String msg, boolean success) {
        lblMessage.setStyle("-fx-text-fill: " + (success ? "#A6E3A1" : "#F38BA8") + ";"
            + "-fx-font-family: Georgia; -fx-font-size: 12px; -fx-font-style: italic;");
        lblMessage.setText(msg);
    }
}
