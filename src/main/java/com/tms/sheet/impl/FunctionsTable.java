package com.tms.sheet.impl;

import com.tms.sheet.AbstractDBTable;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FunctionsTable extends AbstractDBTable {

    private static final List<String> HEADERS = List.of("ID", "NAME", "COLOR", "DISPLAY_ORDER", "EVENT_DATE");

    @Override
    public String getTableName() {
        return "Functions";
    }

    @Override
    public List<String> getColumnHeaders() {
        return HEADERS;
    }

    @Override
    public List<List<Object>> fetchAllRows(Connection conn) throws SQLException {
        ResultSet rs = conn.prepareStatement(
            "SELECT ID, NAME, COLOR, DISPLAY_ORDER, EVENT_DATE FROM Functions ORDER BY ID"
        ).executeQuery();
        List<List<Object>> rows = new ArrayList<>();
        while (rs.next()) {
            String eventDate = rs.getString("EVENT_DATE");
            rows.add(Arrays.asList(
                rs.getInt("ID"),
                rs.getString("NAME"),
                rs.getString("COLOR"),
                rs.getInt("DISPLAY_ORDER"),
                eventDate == null ? "" : eventDate
            ));
        }
        return rows;
    }

    @Override
    public void upsertRows(Connection conn, List<List<Object>> rows) throws SQLException {
        String sql =
            "INSERT INTO Functions (ID, NAME, COLOR, DISPLAY_ORDER, EVENT_DATE) VALUES (?, ?, ?, ?, ?)" +
            " ON DUPLICATE KEY UPDATE NAME=VALUES(NAME), COLOR=VALUES(COLOR)," +
            " DISPLAY_ORDER=VALUES(DISPLAY_ORDER), EVENT_DATE=VALUES(EVENT_DATE)";
        System.out.println("[FunctionsTable] upsertRows called with " + rows.size() + " rows");
        PreparedStatement pre = conn.prepareStatement(sql);
        for (List<Object> row : rows) {
            System.out.println("[FunctionsTable] Binding row: " + row);
            pre.setInt(1, Integer.parseInt(String.valueOf(row.get(0))));
            pre.setString(2, String.valueOf(row.get(1)));
            pre.setString(3, String.valueOf(row.get(2)));
            pre.setInt(4, Integer.parseInt(String.valueOf(row.get(3))));
            String eventDate = row.size() > 4 ? String.valueOf(row.get(4)).trim() : "";
            if (eventDate.isEmpty() || "null".equalsIgnoreCase(eventDate)) {
                pre.setNull(5, java.sql.Types.VARCHAR);
            } else {
                pre.setString(5, eventDate);
            }
            pre.addBatch();
        }
        int[] results = pre.executeBatch();
        System.out.println("[FunctionsTable] executeBatch results length: " + results.length);
        System.out.println("[FunctionsTable] autoCommit=" + conn.getAutoCommit());
    }
}
