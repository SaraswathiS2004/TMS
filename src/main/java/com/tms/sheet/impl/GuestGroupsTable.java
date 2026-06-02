package com.tms.sheet.impl;

import com.tms.sheet.AbstractDBTable;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class GuestGroupsTable extends AbstractDBTable {

    private static final List<String> HEADERS = List.of("ID", "NAME", "COLOR", "DISPLAY_ORDER");

    @Override
    public String getTableName() {
        return "Guest_Groups";
    }

    @Override
    public List<String> getColumnHeaders() {
        return HEADERS;
    }

    @Override
    public List<List<Object>> fetchAllRows(Connection conn) throws SQLException {
        ResultSet rs = conn.prepareStatement(
            "SELECT ID, NAME, COLOR, DISPLAY_ORDER FROM Guest_Groups ORDER BY ID"
        ).executeQuery();
        List<List<Object>> rows = new ArrayList<>();
        while (rs.next()) {
            rows.add(Arrays.asList(
                rs.getInt("ID"),
                rs.getString("NAME"),
                rs.getString("COLOR"),
                rs.getInt("DISPLAY_ORDER")
            ));
        }
        return rows;
    }

    @Override
    public void upsertRows(Connection conn, List<List<Object>> rows) throws SQLException {
        String sql =
            "INSERT INTO Guest_Groups (ID, NAME, COLOR, DISPLAY_ORDER) VALUES (?, ?, ?, ?)" +
            " ON DUPLICATE KEY UPDATE NAME=VALUES(NAME), COLOR=VALUES(COLOR), DISPLAY_ORDER=VALUES(DISPLAY_ORDER)";
        PreparedStatement pre = conn.prepareStatement(sql);
        for (List<Object> row : rows) {
            pre.setInt(1, Integer.parseInt(String.valueOf(row.get(0))));
            pre.setString(2, String.valueOf(row.get(1)));
            pre.setString(3, String.valueOf(row.get(2)));
            pre.setInt(4, Integer.parseInt(String.valueOf(row.get(3))));
            pre.addBatch();
        }
        pre.executeBatch();
    }
}
