package com.tms.sheet.impl;

import com.tms.sheet.AbstractDBTable;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class InvitationPersonFunctionsTable extends AbstractDBTable {

    private static final List<String> HEADERS = List.of("INVITATION_PERSON_ID", "FUNCTION_ID", "INVITED_STATUS");

    @Override
    public String getTableName() {
        return "Invitation_Person_Functions";
    }

    @Override
    public List<String> getColumnHeaders() {
        return HEADERS;
    }

    @Override
    public List<List<Object>> fetchAllRows(Connection conn) throws SQLException {
        ResultSet rs = conn.prepareStatement(
            "SELECT INVITATION_PERSON_ID, FUNCTION_ID, INVITED_STATUS" +
            " FROM Invitation_Person_Functions ORDER BY INVITATION_PERSON_ID, FUNCTION_ID"
        ).executeQuery();
        List<List<Object>> rows = new ArrayList<>();
        while (rs.next()) {
            rows.add(Arrays.asList(
                rs.getInt("INVITATION_PERSON_ID"),
                rs.getInt("FUNCTION_ID"),
                rs.getString("INVITED_STATUS")
            ));
        }
        return rows;
    }

    @Override
    public void upsertRows(Connection conn, List<List<Object>> rows) throws SQLException {
        String sql =
            "INSERT INTO Invitation_Person_Functions (INVITATION_PERSON_ID, FUNCTION_ID, INVITED_STATUS)" +
            " VALUES (?, ?, ?)" +
            " ON DUPLICATE KEY UPDATE INVITED_STATUS=VALUES(INVITED_STATUS)";
        PreparedStatement pre = conn.prepareStatement(sql);
        for (List<Object> row : rows) {
            pre.setInt(1, Integer.parseInt(String.valueOf(row.get(0))));
            pre.setInt(2, Integer.parseInt(String.valueOf(row.get(1))));
            pre.setString(3, String.valueOf(row.get(2)));
            pre.addBatch();
        }
        pre.executeBatch();
    }
}
