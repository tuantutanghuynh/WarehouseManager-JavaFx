package com.warehousemanager.ui.controllers;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import com.warehousemanager.models.entity.FinishedProduct;
import com.warehousemanager.models.entity.Goods;
import com.warehousemanager.models.entity.RawMaterial;
import com.warehousemanager.services.WarehouseService;
import com.warehousemanager.session.UserSession;

import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;

public class GoodsListController implements Initializable {

    @FXML private TableView<Goods> tableView;
    @FXML private TableColumn<Goods, String> colCode;
    @FXML private TableColumn<Goods, String> colName;
    @FXML private TableColumn<Goods, String> colType;
    @FXML private TableColumn<Goods, String> colUnit;
    @FXML private TableColumn<Goods, Integer> colQty;
    @FXML private TableColumn<Goods, String> colExtra;
    @FXML private TableColumn<Goods, Double> colValue;
    @FXML private TextField txtSearch;
    @FXML private ComboBox<String> cbFilter;
    @FXML private Button btnDelete;   // Admin only
    @FXML private Label lblTotal;
    @FXML private Label lblLowCount;
    @FXML private Label lblStatus;

    private final WarehouseService<Goods> service = WarehouseService.getInstance();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        colCode.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getCode()));
        colName.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getName()));
        colUnit.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getUnit()));
        colQty.setCellValueFactory(d -> new SimpleIntegerProperty(d.getValue().getQuantity()).asObject());
        colValue.setCellValueFactory(d -> new SimpleDoubleProperty(d.getValue().calcStockValue()).asObject());

        // Type column — style and label
        colType.setCellValueFactory(d -> new SimpleStringProperty(
            d.getValue() instanceof RawMaterial ? "Raw Material" : "Finished Product"));

        colType.setCellFactory(col -> new TableCell<Goods, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setText(null); setStyle(""); return; }
                setText(item);
                setStyle("Raw Material".equals(item)
                    ? "-fx-text-fill: #FAB387; -fx-font-weight: bold; -fx-font-family: Georgia;"
                    : "-fx-text-fill: #A6E3A1; -fx-font-weight: bold; -fx-font-family: Georgia;");
            }
        });

        // Extra column — Supplier (Raw) or Sell Price (Finished)
        colExtra.setCellValueFactory(d -> {
            if (d.getValue() instanceof RawMaterial rm) {
                return new SimpleStringProperty(rm.getSupplier());
            } else {
                return new SimpleStringProperty(
                    String.format("%.0f VND", ((FinishedProduct) d.getValue()).getSellPrice()));
            }
        });

        // Value column — formatted currency
        colValue.setCellFactory(col -> new TableCell<Goods, Double>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : String.format("%,.2f", item));
            }
        });

        // Qty column — highlight low stock in red
        colQty.setCellFactory(col -> new TableCell<Goods, Integer>() {
            @Override
            protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setText(null); setStyle(""); return; }
                setText(String.valueOf(item));
                Goods g = getTableView().getItems().get(getIndex());
                setStyle(g.IsLow()
                    ? "-fx-text-fill: #F38BA8; -fx-font-weight: bold;"
                    : "-fx-text-fill: #CDD6F4;");
            }
        });

        // Filter combo box
        cbFilter.getItems().addAll("All", "Raw Material (R)", "Finished Product (F)");
        cbFilter.getSelectionModel().selectFirst();

        // UI authorization guard: disable Delete button for non-admin users
        btnDelete.setDisable(!UserSession.isAdmin());

        loadData(service.getAll());
    }

    private void loadData(List<Goods> list) {
        tableView.setItems(FXCollections.observableArrayList(list));
        updateStats(list);
        lblStatus.setText("");
    }

    private void updateStats(List<Goods> list) {
        // Calculate total stock value asynchronously
        service.calcTotalStockValueAsync(total ->
            lblTotal.setText(String.format("Total Stock Value: %,.2f VND", total)));

        long lowCount = list.stream().filter(Goods::IsLow).count();
        lblLowCount.setText("Low stock: " + lowCount + " items");
        if (lowCount > 0) {
            lblLowCount.setStyle("-fx-text-fill: #F38BA8; -fx-font-family: Georgia;");
        } else {
            lblLowCount.setStyle("-fx-text-fill: #A6E3A1; -fx-font-family: Georgia;");
        }
    }

    @FXML
    private void handleFilter() {
        String selected = cbFilter.getValue();
        List<Goods> result;
        if (selected == null || selected.startsWith("All")) {
            result = service.getAll();
        } else if (selected.contains("(R)")) {
            result = service.filterByType("R");
        } else {
            result = service.filterByType("F");
        }
        tableView.setItems(FXCollections.observableArrayList(result));
        updateStats(result);
        lblStatus.setText("Showing: " + result.size() + " items.");
    }

    @FXML
    private void handleSearch() {
        String keyword = txtSearch.getText().trim().toLowerCase();
        if (keyword.isEmpty()) {
            loadData(service.getAll());
            return;
        }
        List<Goods> all = service.getAll();
        List<Goods> result = new ArrayList<>();
        for (Goods g : all) {
            if (g.getCode().toLowerCase().contains(keyword)
             || g.getName().toLowerCase().contains(keyword)) {
                result.add(g);
            }
        }
        tableView.setItems(FXCollections.observableArrayList(result));
        updateStats(result);
        lblStatus.setText("Found " + result.size() + " results for: \"" + keyword + "\"");
    }

    @FXML
    private void handleClearSearch() {
        txtSearch.clear();
        cbFilter.getSelectionModel().selectFirst();
        loadData(service.getAll());
    }

    @FXML
    private void handleSort() {
        service.sortAndDisplayAsync(sorted -> {
            tableView.setItems(FXCollections.observableArrayList(sorted));
            updateStats(sorted);
            lblStatus.setText("Sorted by quantity descending.");
        });
    }

    @FXML
    private void handleExport() {
        Goods selected = tableView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            lblStatus.setText("Select a row to export goods.");
            return;
        }

        TextInputDialog dialog = new TextInputDialog("1");
        dialog.setTitle("Export Goods");
        dialog.setHeaderText("Export item: " + selected.getCode() + " — " + selected.getName());
        dialog.setContentText("Export quantity:");

        dialog.showAndWait().ifPresent(input -> {
            try {
                int qty = Integer.parseInt(input.trim());
                if (qty <= 0) {
                    showStatusError("Quantity must be > 0.");
                    return;
                }

                service.exportAsync(selected.getCode(), qty, errMsg -> {
                    if (errMsg == null) {
                        loadData(service.getAll());
                        lblStatus.setText("Exported successfully: " + selected.getCode() + " x" + qty);
                    } else {
                        showStatusError("Export error: " + errMsg);
                    }
                });

            } catch (NumberFormatException e) {
                showStatusError("Invalid quantity.");
            }
        });
    }

    @FXML
    private void handleShowLowStock() {
        List<Goods> low = service.findLowStock();
        tableView.setItems(FXCollections.observableArrayList(low));
        updateStats(low);
        lblStatus.setText("Warning: " + low.size() + " low stock items.");
    }

    @FXML
    private void handleDelete() {
        // Second-layer authorization check: ensure user is Admin
        if (!UserSession.isAdmin()) {
            showStatusError("Only admin can delete goods.");
            return;
        }

        Goods selected = tableView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            lblStatus.setText("Select a row to delete.");
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION,
            "Delete \"" + selected.getName() + "\" (" + selected.getCode() + ")?",
            ButtonType.YES, ButtonType.NO);
        alert.setTitle("Confirm Delete");
        alert.setHeaderText(null);
        alert.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.YES) {
                if (service.delete(selected.getCode())) {
                    loadData(service.getAll());
                    lblStatus.setText("Deleted: " + selected.getName());
                } else {
                    showStatusError("Delete failed. Check DB.");
                }
            }
        });
    }

    @FXML
    private void handleReload() {
        lblStatus.setText("Reloading from DB...");
        service.loadFromDBAsync(() -> {
            loadData(service.getAll());
            lblStatus.setText("Reloaded from database.");
        });
    }

    @FXML
    private void handleBackup() {
        lblStatus.setText("Backing up...");
        service.saveToFileAsync(WarehouseService.DEFAULT_BACKUP_FILE, () ->
            lblStatus.setText("Backed up to " + WarehouseService.DEFAULT_BACKUP_FILE));
    }

    @FXML
    private void handleRestore() {
        lblStatus.setText("Restoring...");
        service.loadFromFileAsync(WarehouseService.DEFAULT_BACKUP_FILE, () -> {
            loadData(service.getAll());
            lblStatus.setText("Restored from " + WarehouseService.DEFAULT_BACKUP_FILE
                + ". Note: not saved to DB, please import again if permanent saving is needed.");
        });
    }

    private void showStatusError(String msg) {
        lblStatus.setStyle("-fx-text-fill: #F38BA8; -fx-font-family: Georgia;");
        lblStatus.setText(msg);
    }

    @FXML
    private void goBack() throws IOException {
        SceneSwitcher.switchScene("dashboard.fxml");
    }
}
