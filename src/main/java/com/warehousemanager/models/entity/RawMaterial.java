package com.warehousemanager.models.entity;

import java.util.Scanner;

public class RawMaterial extends Goods {

    private String supplier;

    public String getSupplier() {
        return supplier;
    }
    
    public void setSupplier(String supplier) {
        this.supplier = supplier;
    }

    @Override
    public double calcStockValue() {
       return getQuantity() *1.2;
    }

    @Override
    public String getTypeCode() {
        return "R";
    }

    @Override
    public void Input(Scanner sc) {
        // TODO Auto-generated method stub
        super.Input(sc);
        System.out.print("Suppliers: ");
        supplier = sc.nextLine().trim();
    }

    @Override
    public void PrintInfo() {
        System.out.printf("[NGUYEN LIEU] %s | %s | SL: %d %s | Nha CC: %s | Gia tri kho: %.2f%n",
            getCode(), getName(), getQuantity(), getUnit(), supplier, calcStockValue());
    }
}

    

