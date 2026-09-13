package com.warehousemanager.config;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

// Central place holding the SQL Server connection settings and the single
// entry point used to obtain a Connection. Deliberately does NOT cache/reuse
// one static Connection: JDBC Connections aren't guaranteed thread-safe, and
// WarehouseService runs several DB operations on background threads at once.
// Opening a fresh Connection per call (closed by the caller via
// try-with-resources) avoids two threads ever sharing the same Connection.
//
// Credentials are NOT hardcoded here — they're loaded from db.properties on
// the classpath (src/main/resources/db.properties), which is gitignored so
// real credentials never get committed. Copy db.properties.example to
// db.properties and fill in your own values to run the app locally.
public class DatabaseConfig {

    private static final String URL;
    private static final String USER;
    private static final String PASSWORD;

    static {
        Properties props = new Properties();
        try (InputStream in = DatabaseConfig.class.getResourceAsStream("/db.properties")) {
            if (in == null) {
                throw new IllegalStateException(
                        "Missing src/main/resources/db.properties — copy db.properties.example "
                                + "to db.properties and fill in your real DB credentials.");
            }
            props.load(in);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to read db.properties.", e);
        }
        URL = props.getProperty("db.url");
        USER = props.getProperty("db.user");
        PASSWORD = props.getProperty("db.password");
    }

    // Static utility class — never instantiated.
    private DatabaseConfig() {}

    // Opens a brand new Connection. Caller is responsible for closing it
    // (use try-with-resources).
    public static Connection getConnection() throws SQLException{
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}
