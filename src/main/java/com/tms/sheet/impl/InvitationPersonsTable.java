package com.tms.sheet.impl;

import com.tms.sheet.AbstractDBTable;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class InvitationPersonsTable extends AbstractDBTable {

    private static final List<String> HEADERS = List.of("ID", "INVITATION_ID", "NAME", "NOTE");

    @Override
    public String getTableName() {
        return "Invitation_Persons";
    }

    @Override
    public List<String> getColumnHeaders() {
        return HEADERS;
    }

    @Override
    public List<List<Object>> fetchAllRows(Connection conn) throws SQLException {
        ResultSet rs = conn.prepareStatement(
            "SELECT ID, INVITATION_ID, NAME, NOTE FROM Invitation_Persons ORDER BY INVITATION_ID, ID"
        ).executeQuery();
        List<List<Object>> rows = new ArrayList<>();
        while (rs.next()) {
            rows.add(Arrays.asList(
                rs.getInt("ID"),
                rs.getInt("INVITATION_ID"),
                rs.getString("NAME"),
                rs.getString("NOTE")
            ));
        }
        return rows;
    }

    @Override
    public void upsertRows(Connection conn, List<List<Object>> rows) throws SQLException {
        String sql =
            "INSERT INTO Invitation_Persons (ID, INVITATION_ID, NAME, NOTE) VALUES (?, ?, ?, ?)" +
            " ON DUPLICATE KEY UPDATE INVITATION_ID=VALUES(INVITATION_ID), NAME=VALUES(NAME), NOTE=VALUES(NOTE)";
        PreparedStatement pre = conn.prepareStatement(sql);
        for (List<Object> row : rows) {
            pre.setInt(1, Integer.parseInt(String.valueOf(row.get(0))));
            pre.setInt(2, Integer.parseInt(String.valueOf(row.get(1))));
            pre.setString(3, String.valueOf(row.get(2)));
            pre.setString(4, String.valueOf(row.get(3)));
            pre.addBatch();
        }
        pre.executeBatch();
    }
}
