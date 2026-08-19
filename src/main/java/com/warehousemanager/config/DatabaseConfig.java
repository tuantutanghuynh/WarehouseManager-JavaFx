package com.warehousemanager.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

// Central place holding the SQL Server connection settings and the single
// entry point used to obtain a Connection. Deliberately does NOT cache/reuse
// one static Connection: JDBC Connections aren't guaranteed thread-safe, and
// WarehouseService runs several DB operations on background threads at once.
// Opening a fresh Connection per call (closed by the caller via
// try-with-resources) avoids two threads ever sharing the same Connection.
public class DatabaseConfig {

    private static final String URL      = "jdbc:sqlserver://localhost:1433;databaseName=WarehouseManagerDB;encrypt=false";
    private static final String USER     = "sa";
    private static final String PASSWORD = "...";

    // Static utility class — never instantiated.
    private DatabaseConfig() {}

    // Opens a brand new Connection. Caller is responsible for closing it
    // (use try-with-resources).
    public static Connection getConnection() throws SQLException{
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}
