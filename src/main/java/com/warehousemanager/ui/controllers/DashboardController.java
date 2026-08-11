package com.warehousemanager.ui.controllers;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import com.warehousemanager.services.WarehouseService;
import com.warehousemanager.session.UserSession;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;

public class DashboardController implements Initializable {

    @FXML
    private Label lblWelcome;
    @FXML
    private Label lblRole;
    @FXML
    private Label lblStatus;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        lblWelcome.setText("Xin chao, " + UserSession.get().getUsername());
        lblRole.setText("Quyen: " + UserSession.get().getRole().toUpperCase());
        lblStatus.setText("Dang tai du lieu kho...");

        // Load cache ở background thread — không làm đơ giao diện
        WarehouseService.getInstance().loadFromDBAsync(() -> {
            int count = WarehouseService.getInstance().getAll().size();
            lblStatus.setText(count + " mat hang da tai.");
        });
    }

    @FXML
    private void goAddGoods() throws IOException {
        SceneSwitcher.switchScene("add_goods.fxml");
    }

    @FXML
    private void goGoodsList() throws IOException {
        SceneSwitcher.switchScene("goods_list.fxml");
    }

    @FXML
    private void handleLogout() throws IOException {
        UserSession.clear();
        WarehouseService.reset();
        SceneSwitcher.switchScene("login.fxml");
    }
}
