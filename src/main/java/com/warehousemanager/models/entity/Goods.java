package com.warehousemanager.models.entity;

import java.io.Serializable;
import java.util.Scanner;

// Base class for every item stored in the warehouse (RawMaterial, FinishedProduct).
// Holds the fields every item has in common, encapsulated behind getters/setters,
// and implements IsLow()/Input()/PrintInfo() once so subclasses don't repeat them.
// Serializable so a list of Goods can be written to/read from a backup file.
// calcStockValue()/getTypeCode() are left abstract: each subclass values stock
// differently and needs its own "R"/"F" discriminator for the database.
public abstract class Goods implements IGoods, Serializable  {

    private static final long serialVersionUID = 1L;

    private String code;
    private String name;
    private String unit;
    private int quantity;
    private int minStockLevel;

    // Item code, primary key in the Goods table (e.g. "RM001").
    public String getCode(){
        return code;
    }

    public void setCode(String code){
        this.code = code;
    }

    // Item display name.
    public String getName(){
        return name;
    }

    public void setName(String name){
        this.name = name;
    }

    // Unit of measure (Kg, Goi, Hop...).
    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    // Current stock quantity on hand.
    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    // Per-item low-stock warning threshold (compared against quantity in IsLow()).
    public int getMinStockLevel() {
        return minStockLevel;
    }

    public void setMinStockLevel(int minStockLevel) {
        this.minStockLevel = minStockLevel;
    }

    // Stock value formula differs per subclass (raw material vs finished product).
    public abstract double calcStockValue();

    // Discriminator used by the repository to store/rebuild the correct subclass ("R"/"F").
    public abstract String getTypeCode();

    // Read the fields common to every item type. Subclasses call this via
    // super.Input(sc) first, then prompt for their own extra field.
    @Override
    public void Input(Scanner sc) {
        System.out.print("Product code: ");
        code = sc.nextLine().trim();
        System.out.print("Product name: ");
        name = sc.nextLine().trim();
        System.out.print("Unit: ");
        unit = sc.nextLine().trim();
        System.out.print("Quantity: ");
        quantity = Integer.parseInt(sc.nextLine().trim());
        System.out.print("Low inventory warning threshold:");
        minStockLevel = Integer.parseInt(sc.nextLine().trim());      
    }

    // Shared for every subclass: low stock means quantity fell below this item's
    // own minStockLevel (no more hardcoded threshold per type).
    @Override
    public boolean IsLow() {
        return quantity < minStockLevel;
    }

    // Default console representation; subclasses override to show their extra field.
    @Override
    public void PrintInfo() {
        System.out.printf("[%s] %s | %s | SL: %d %s | Nguong: %d | Gia tri: %.2f%n",
            getTypeCode(), code, name, quantity, unit, minStockLevel, calcStockValue());
        
    }

   
    
}
