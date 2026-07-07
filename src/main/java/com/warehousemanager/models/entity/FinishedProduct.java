package com.warehousemanager.models.entity;

import java.util.Scanner;

public class FinishedProduct extends  Goods {

    private double sellPrice;

    public double getSellPrice() {
        return sellPrice;
    }
    public void setSellPrice(double sellPrice) {
        this.sellPrice = sellPrice;
    }

    @Override
    public double calcStockValue() {
        return getQuantity()*sellPrice;
    }

    @Override
    public String getTypeCode() {
        return "F";
    }

    @Override
    public void Input(Scanner sc) {
        // TODO Auto-generated method stub
        super.Input(sc);
        System.out.print("Price: ");
        sellPrice = Double.parseDouble(sc.nextLine().trim());
    }

    @Override
    public void PrintInfo() {
        System.out.printf("[THANH PHAM] %s | %s | SL: %d %s | Gia ban: %.2f | Gia tri kho: %.2f%n",
            getCode(), getName(), getQuantity(), getUnit(), sellPrice, calcStockValue());
    }
    
}
