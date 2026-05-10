package com.tms.data.respository;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class TmsDB {

    private static final String DB_URL = "jdbc:mysql://localhost:3306/Tms";
    private static final String DB_USER = "root";
    private static final String DB_PASS = "";

    private static TmsDB tmsdb;
    Connection con;

    private TmsDB() throws ClassNotFoundException, SQLException {
        Class.forName("com.mysql.cj.jdbc.Driver");
        con = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
        SchemaInit.init(con);
    }

    public static TmsDB getInstance() throws ClassNotFoundException, SQLException {
        if (tmsdb == null) {
            tmsdb = new TmsDB();
        }
        return tmsdb;
    }

    public Connection getConnection() {
        return con;
    }

    /** Opens a fresh connection for background threads (sheet sync, restore). Caller must close it. */
    public static Connection openConnection() throws ClassNotFoundException, SQLException {
        Class.forName("com.mysql.cj.jdbc.Driver");
        return DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
    }
}
