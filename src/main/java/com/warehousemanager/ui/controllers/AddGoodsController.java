package com.warehousemanager.ui.controllers;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import com.warehousemanager.models.entity.FinishedProduct;
import com.warehousemanager.models.entity.Goods;
import com.warehousemanager.models.entity.RawMaterial;
import com.warehousemanager.services.WarehouseService;
import com.warehousemanager.utils.Validator;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.HBox;

public class AddGoodsController implements Initializable {

    @FXML private TextField txtCode;
    @FXML private TextField txtName;
    @FXML private TextField txtUnit;
    @FXML private TextField txtQuantity;
    @FXML private TextField txtMinStock;
    @FXML private ToggleButton btnRawMaterial;
    @FXML private ToggleButton btnFinishedProduct;
    @FXML private HBox rowSupplier;
    @FXML private HBox rowSellPrice;
    @FXML private TextField txtSupplier;
    @FXML private TextField txtSellPrice;
    @FXML private Label lblMessage;

    private final ToggleGroup typeGroup = new ToggleGroup();
    private final WarehouseService<Goods> service = WarehouseService.getInstance();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        btnRawMaterial.setToggleGroup(typeGroup);
        btnFinishedProduct.setToggleGroup(typeGroup);
        btnRawMaterial.setSelected(true); // Default to Raw Material

        // Hide Sell Price row by default
        rowSellPrice.setVisible(false);
        rowSellPrice.setManaged(false);

        // Toggle listener for dynamic input fields
        typeGroup.selectedToggleProperty().addListener((obs, old, val) -> {
            boolean isRaw = (val == btnRawMaterial);
            rowSupplier.setVisible(isRaw);
            rowSupplier.setManaged(isRaw);
            rowSellPrice.setVisible(!isRaw);
            rowSellPrice.setManaged(!isRaw);
        });

        lblMessage.setText("");
    }

    @FXML
    private void handleImport() {
        try {
            String code = txtCode.getText().trim();
            String name = txtName.getText().trim();
            String unit = txtUnit.getText().trim();
            String qtyStr = txtQuantity.getText().trim();

            Validator.requireGoodsCode(code, "Goods Code");
            Validator.requireNonBlank(name, "Goods Name");
            Validator.requireNonBlank(unit, "Unit");
            int qty = Validator.parsePositiveInt(qtyStr, "Quantity");
            int minStock = Validator.parseNonNegativeInt(txtMinStock.getText().trim(), "Min Stock Level");

            Goods g;
            if (btnRawMaterial.isSelected()) {
                String supplier = txtSupplier.getText().trim();
                Validator.requireNonBlank(supplier, "Supplier");

                RawMaterial rm = new RawMaterial();
                rm.setCode(code);
                rm.setName(name);
                rm.setUnit(unit);
                rm.setQuantity(qty);
                rm.setMinStockLevel(minStock);
                rm.setSupplier(supplier);
                g = rm;
            } else {
                double price = Validator.parsePositiveDouble(txtSellPrice.getText().trim(), "Sell Price");

                FinishedProduct fp = new FinishedProduct();
                fp.setCode(code);
                fp.setName(name);
                fp.setUnit(unit);
                fp.setQuantity(qty);
                fp.setMinStockLevel(minStock);
                fp.setSellPrice(price);
                g = fp;
            }

            // Async import
            service.importAsync(g, ok -> {
                if (ok) {
                    showMsg("Imported successfully: " + g.getCode(), true);
                    clearForm();
                } else {
                    showMsg("Import failed. Check DB connection.", false);
                }
            });

        } catch (IllegalArgumentException e) {
            showMsg(e.getMessage(), false);
        }
    }

    private void clearForm() {
        txtCode.clear();
        txtName.clear();
        txtUnit.clear();
        txtQuantity.clear();
        txtMinStock.clear();
        txtSupplier.clear();
        txtSellPrice.clear();
        btnRawMaterial.setSelected(true);
    }

    private void showMsg(String msg, boolean success) {
        lblMessage.setStyle("-fx-text-fill: " + (success ? "#A6E3A1" : "#F38BA8") + ";"
            + "-fx-font-family: Georgia; -fx-font-size: 12px; -fx-font-style: italic;");
        lblMessage.setText(msg);
    }

    @FXML
    private void goBack() throws IOException {
        SceneSwitcher.switchScene("dashboard.fxml");
    }
}
