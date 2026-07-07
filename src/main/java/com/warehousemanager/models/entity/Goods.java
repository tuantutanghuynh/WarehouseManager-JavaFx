package com.warehousemanager.models.entity;

import java.io.Serializable;
import java.util.Scanner;

public abstract class Goods implements IGoods, Serializable  {

    private static final long serialVersionUID = 1L;

    private String code;
    private String name;
    private String unit;
    private int quantity;
    private int minStockLevel;

    public String getCode(){
        return code;
    }

    public void setCode(String code){
        this.code = code;
    }
    
    public String getName(){
        return name;
    }
    
    public void setName(String name){
        this.name = name;    
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public int getMinStockLevel() {
        return minStockLevel;
    }

    public void setMinStockLevel(int minStockLevel) {
        this.minStockLevel = minStockLevel;
    }

    public abstract double calcStockValue();
    public abstract String getTypeCode();

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

    @Override
    public boolean IsLow() {
        return quantity < minStockLevel;
    }

    @Override
    public void PrintInfo() {
        System.out.printf("[%s] %s | %s | SL: %d %s | Nguong: %d | Gia tri: %.2f%n",
            getTypeCode(), code, name, quantity, unit, minStockLevel, calcStockValue());
        
    }

   
    
}
