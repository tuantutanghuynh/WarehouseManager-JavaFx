package com.warehousemanager.models.entity;

public class Warehouse {

    private Goods[] stock = new Goods[100];
    private int count = 0;

    // Add a new goods item to the warehouse stock
    public void Import(Goods g) {
        if (count >= stock.length) {
            System.out.println("Stock is full");
            return;
        }

        for (int i = 0; i < count; i++) {
            if (stock[i].getCode().equalsIgnoreCase(g.getCode())) {
                stock[i].setQuantity(stock[i].getQuantity() + g.getQuantity());
                System.out.println("Updated quantity: " + stock[i].getCode());
                return;
            }
        }
        stock[count] = g;
        count++;
        System.out.println("Imported successfully: " + g.getCode());
    }

    // xuất hàng khỏi kho
    public void Export(String code, int qty) {
        boolean found = false;
        try {
            for (int i = 0; i < count; i++) {
                if (stock[i].getCode().equalsIgnoreCase(code)) {
                    found = true;
                    if (qty <= 0) {
                        throw new IllegalArgumentException("Export quantity must be > 0");
                    }
                    if (qty > stock[i].getQuantity()) {
                        throw new IllegalStateException(
                                "Not enough stock: required " + qty + ", avalable " + stock[i].getQuantity());
                    }
                    stock[i].setQuantity(stock[i].getQuantity() - qty);
                    System.out.println("Exported successfully: " + code + " x" + qty);
                    return;
                }
            }
            if (!found) {
                throw new IllegalArgumentException("Good not foun:" + code);
            }
        } finally {
            System.out.println("[LOG] Export goods: code=" + code + ", qty=" + qty);
        }
    }

    // Hiển thị toàn bộ kho
    public void Show() {
        if (count == 0) {
            System.out.println("Warehouse is empty.");
            return;
        }
        System.out.println("=== WAREHOUSE GOODS LIST ===");
        for (int i = 0; i < count; i++)
            stock[i].PrintInfo();
        System.out.println("==========================");
    }

    // Cảnh báo mặt hàng tồn kho thấp
    public void CheckLowStock() {
        System.out.println("=== LOW STOCK WARNING ===");
        boolean hasLow = false;
        for (int i = 0; i < count; i++) {
            if (stock[i].IsLow()) {
                stock[i].PrintInfo();
                hasLow = true;
            }
        }
        if (!hasLow)
            System.out.println("All items have sufficient stock.");
        System.out.println("==============================");
    }
}
