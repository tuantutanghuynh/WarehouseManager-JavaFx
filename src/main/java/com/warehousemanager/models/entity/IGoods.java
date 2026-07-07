package com.warehousemanager.models.entity;

import java.util.Scanner;

public interface IGoods {
    void Input(Scanner sc);
    void PrintInfo();
    boolean IsLow();
}
