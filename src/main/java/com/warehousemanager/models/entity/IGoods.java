package com.warehousemanager.models.entity;

import java.util.Scanner;

// Behavior contract shared by every kind of goods in the warehouse.
// Any class implementing this interface must provide console I/O and a
// low-stock check, regardless of how it stores its data internally.
// Kept separate from the Goods abstract class so unrelated classes could
// also adopt this contract without inheriting Goods' fields.
public interface IGoods {

    // Read one item's data from the keyboard (console demo use case).
    void Input(Scanner sc);

    // Print this item's info to the console.
    void PrintInfo();

    // Return true when quantity has fallen below the item's warning threshold.
    boolean IsLow();
}
