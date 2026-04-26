package com.tms.data.respository;

import java.sql.Connection;
import java.sql.Statement;

public class SchemaInit {

    public static void init(Connection conn) {
        try (Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(
                "CREATE TABLE IF NOT EXISTS Functions (" +
                "  ID INT NOT NULL AUTO_INCREMENT," +
                "  NAME VARCHAR(100) NOT NULL," +
                "  COLOR VARCHAR(30) DEFAULT '#4f46e5'," +
                "  DISPLAY_ORDER INT DEFAULT 0," +
                "  PRIMARY KEY (ID)" +
                ")"
            );
            stmt.executeUpdate(
                "CREATE TABLE IF NOT EXISTS Person_Functions (" +
                "  PERSON_ID INT NOT NULL," +
                "  FUNCTION_ID INT NOT NULL," +
                "  PRIMARY KEY (PERSON_ID, FUNCTION_ID)," +
                "  FOREIGN KEY (PERSON_ID) REFERENCES Invitations(ID) ON DELETE CASCADE," +
                "  FOREIGN KEY (FUNCTION_ID) REFERENCES Functions(ID) ON DELETE CASCADE" +
                ")"
            );
        } catch (Exception e) {
            System.out.println("SchemaInit error: " + e.getMessage());
        }
    }
}
