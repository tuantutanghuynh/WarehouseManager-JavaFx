package com.warehousemanager.models.entity;

import java.util.Scanner;

// Finished product goods (roasted coffee, instant coffee, bottled coffee...) —
// stock value uses the item's real sell price instead of a fixed cost factor.
public class FinishedProduct extends  Goods {

    private double sellPrice;

    // Sell price — only finished products have this field.
    public double getSellPrice() {
        return sellPrice;
    }
    public void setSellPrice(double sellPrice) {
        this.sellPrice = sellPrice;
    }

    // Stock value = quantity * sell price.
    @Override
    public double calcStockValue() {
        return getQuantity()*sellPrice;
    }

    // Discriminator stored in the GoodsType column.
    @Override
    public String getTypeCode() {
        return "F";
    }

    // Read common fields via super, then the sell price specific to finished products.
    @Override
    public void Input(Scanner sc) {
        super.Input(sc);
        System.out.print("Price: ");
        sellPrice = Double.parseDouble(sc.nextLine().trim());
    }

    // Console representation including sell price and computed stock value.
    @Override
    public void PrintInfo() {
        System.out.printf("[THANH PHAM] %s | %s | SL: %d %s | Gia ban: %.2f | Gia tri kho: %.2f%n",
            getCode(), getName(), getQuantity(), getUnit(), sellPrice, calcStockValue());
    }
    
}
