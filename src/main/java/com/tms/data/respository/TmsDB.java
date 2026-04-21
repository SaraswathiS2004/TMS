package com.tms.data.respository;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class TmsDB {

    private static TmsDB tmsdb;
    Connection con;
    private TmsDB() throws ClassNotFoundException, SQLException {
        Class.forName("com.mysql.cj.jdbc.Driver");
        String url = "jdbc:mysql://localhost:3306/Tms";
        String user = "root";
        String password = "Sarah@2004";
        con = DriverManager.getConnection(url, user, password);
    }

    public static TmsDB getInstance() throws ClassNotFoundException, SQLException{
        if(tmsdb == null){
            tmsdb = new TmsDB();
        }
        return tmsdb;
    }

    public  Connection getConnection() {
        return con;
    }
}
