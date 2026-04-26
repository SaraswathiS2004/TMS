package com.tms.data.respository;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

public class SchemaInit {

    public static void init(Connection conn) {
        try (Statement stmt = conn.createStatement()) {

            // 1. Invitations — must exist before the join table references it
            stmt.executeUpdate(
                "CREATE TABLE IF NOT EXISTS Invitations (" +
                "  ID                         INT          NOT NULL AUTO_INCREMENT," +
                "  NAME                       VARCHAR(100) NOT NULL," +
                "  CITY                       VARCHAR(100) NOT NULL DEFAULT ''," +
                "  RELATION_TYPE              VARCHAR(20)  NOT NULL DEFAULT 'CLOSE'," +
                "  NUMBER_OF_PEOPLE_WILL_COME INT          NOT NULL DEFAULT 1," +
                "  INVITED_STATUS             VARCHAR(30)  NOT NULL DEFAULT 'NOT_INVITED'," +
                "  PRIMARY KEY (ID)" +
                ")"
            );

            // 2. Functions — dynamic event categories (e.g. Marriage, Engagement)
            stmt.executeUpdate(
                "CREATE TABLE IF NOT EXISTS Functions (" +
                "  ID            INT          NOT NULL AUTO_INCREMENT," +
                "  NAME          VARCHAR(100) NOT NULL," +
                "  COLOR         VARCHAR(30)  NOT NULL DEFAULT '#4f46e5'," +
                "  DISPLAY_ORDER INT          NOT NULL DEFAULT 0," +
                "  PRIMARY KEY (ID)" +
                ")"
            );

            // 3. Person_Functions — many-to-many: who is invited to which function
            stmt.executeUpdate(
                "CREATE TABLE IF NOT EXISTS Person_Functions (" +
                "  PERSON_ID      INT         NOT NULL," +
                "  FUNCTION_ID    INT         NOT NULL," +
                "  INVITED_STATUS VARCHAR(30) NOT NULL DEFAULT 'NOT_INVITED'," +
                "  PRIMARY KEY (PERSON_ID, FUNCTION_ID)," +
                "  CONSTRAINT fk_pf_person   FOREIGN KEY (PERSON_ID)   REFERENCES Invitations(ID) ON DELETE CASCADE," +
                "  CONSTRAINT fk_pf_function FOREIGN KEY (FUNCTION_ID) REFERENCES Functions(ID)   ON DELETE CASCADE" +
                ")"
            );

            System.out.println("SchemaInit: all tables verified / created.");
        } catch (Exception e) {
            System.out.println("SchemaInit error: " + e.getMessage());
        }

        // Migration: add INVITED_STATUS to Person_Functions for existing databases
        try {
            boolean columnExists;
            try (Statement chk = conn.createStatement()) {
                ResultSet rs = chk.executeQuery(
                    "SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS" +
                    " WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'Person_Functions'" +
                    " AND COLUMN_NAME = 'INVITED_STATUS'"
                );
                rs.next();
                columnExists = rs.getInt(1) > 0;
            }
            if (!columnExists) {
                try (Statement alter = conn.createStatement()) {
                    alter.executeUpdate(
                        "ALTER TABLE Person_Functions" +
                        " ADD COLUMN INVITED_STATUS VARCHAR(30) NOT NULL DEFAULT 'NOT_INVITED'"
                    );
                    System.out.println("SchemaInit: migrated Person_Functions — added INVITED_STATUS.");
                }
            }
        } catch (Exception e) {
            System.out.println("SchemaInit migration error: " + e.getMessage());
        }
    }
}
