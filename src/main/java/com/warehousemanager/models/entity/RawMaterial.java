package com.warehousemanager.models.entity;

import java.util.Scanner;

// Raw material goods (coffee beans, milk, sugar...) — stock value is estimated
// as quantity * a fixed 1.2 cost factor instead of tracking real purchase price.
public class RawMaterial extends Goods {

    private String supplier;

    // Supplier name — only raw materials have this field.
    public String getSupplier() {
        return supplier;
    }

    public void setSupplier(String supplier) {
        this.supplier = supplier;
    }

    // Stock value = quantity * cost factor (1.2).
    @Override
    public double calcStockValue() {
       return getQuantity() *1.2;
    }

    // Discriminator stored in the GoodsType column.
    @Override
    public String getTypeCode() {
        return "R";
    }

    // Read common fields via super, then the supplier field specific to raw material.
    @Override
    public void Input(Scanner sc) {
        super.Input(sc);
        System.out.print("Suppliers: ");
        supplier = sc.nextLine().trim();
    }

    // Console representation including supplier and computed stock value.
    @Override
    public void PrintInfo() {
        System.out.printf("[NGUYEN LIEU] %s | %s | SL: %d %s | Nha CC: %s | Gia tri kho: %.2f%n",
            getCode(), getName(), getQuantity(), getUnit(), supplier, calcStockValue());
    }
}

    

