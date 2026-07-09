package com.warehousemanager.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConfig {

    private static final String URL      = "jdbc:sqlserver://localhost:1433;databaseName=WarehouseManagerDB;encrypt=false";
    private static final String USER     = "sa";
    private static final String PASSWORD = "tuantu209423";

    private DatabaseConfig() {}

    public static Connection getConnection() throws SQLException{
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}
