package com.tms.sheet.impl;

import com.tms.sheet.AbstractDBTable;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class InvitationsTable extends AbstractDBTable {

    private static final List<String> HEADERS = List.of(
        "ID", "NAME", "CITY", "RELATION_TYPE", "NUMBER_OF_PEOPLE_WILL_COME", "INVITED_STATUS", "GROUP_ID"
    );

    @Override
    public String getTableName() {
        return "Invitations";
    }

    @Override
    public List<String> getColumnHeaders() {
        return HEADERS;
    }

    @Override
    public List<List<Object>> fetchAllRows(Connection conn) throws SQLException {
        PreparedStatement pre = conn.prepareStatement(
            "SELECT ID, NAME, CITY, RELATION_TYPE, NUMBER_OF_PEOPLE_WILL_COME, INVITED_STATUS, GROUP_ID" +
            " FROM Invitations ORDER BY ID"
        );
        ResultSet rs = pre.executeQuery();
        List<List<Object>> rows = new ArrayList<>();
        while (rs.next()) {
            Object groupId = rs.getObject("GROUP_ID");
            rows.add(Arrays.asList(
                rs.getInt("ID"),
                rs.getString("NAME"),
                rs.getString("CITY"),
                rs.getString("RELATION_TYPE"),
                rs.getInt("NUMBER_OF_PEOPLE_WILL_COME"),
                rs.getString("INVITED_STATUS"),
                groupId == null ? "" : groupId
            ));
        }
        return rows;
    }

    @Override
    public void upsertRows(Connection conn, List<List<Object>> rows) throws SQLException {
        String sql =
            "INSERT INTO Invitations (ID, NAME, CITY, RELATION_TYPE, NUMBER_OF_PEOPLE_WILL_COME, INVITED_STATUS, GROUP_ID)" +
            " VALUES (?, ?, ?, ?, ?, ?, ?)" +
            " ON DUPLICATE KEY UPDATE" +
            "  NAME=VALUES(NAME), CITY=VALUES(CITY), RELATION_TYPE=VALUES(RELATION_TYPE)," +
            "  NUMBER_OF_PEOPLE_WILL_COME=VALUES(NUMBER_OF_PEOPLE_WILL_COME), INVITED_STATUS=VALUES(INVITED_STATUS)," +
            "  GROUP_ID=VALUES(GROUP_ID)";
        System.out.println("[InvitationsTable] upsertRows called with " + rows.size() + " rows");
        PreparedStatement pre = conn.prepareStatement(sql);
        for (List<Object> row : rows) {
            System.out.println("[InvitationsTable] Binding row: " + row);
            pre.setInt(1, Integer.parseInt(String.valueOf(row.get(0))));
            pre.setString(2, String.valueOf(row.get(1)));
            pre.setString(3, String.valueOf(row.get(2)));
            pre.setString(4, String.valueOf(row.get(3)));
            pre.setInt(5, Integer.parseInt(String.valueOf(row.get(4))));
            pre.setString(6, String.valueOf(row.get(5)));
            String groupId = row.size() > 6 ? String.valueOf(row.get(6)).trim() : "";
            if (groupId.isEmpty() || "0".equals(groupId) || "null".equalsIgnoreCase(groupId)) {
                pre.setNull(7, java.sql.Types.INTEGER);
            } else {
                pre.setInt(7, Integer.parseInt(groupId));
            }
            pre.addBatch();
        }
        int[] results = pre.executeBatch();
        System.out.println("[InvitationsTable] executeBatch results length: " + results.length);
        System.out.println("[InvitationsTable] autoCommit=" + conn.getAutoCommit());
    }
}
