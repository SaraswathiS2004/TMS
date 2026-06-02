package com.tms.sheet.impl;

import com.tms.sheet.AbstractDBTable;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class BudgetItemsTable extends AbstractDBTable {

    private static final List<String> HEADERS = List.of(
        "ID", "FUNCTION_ID", "NAME", "ESTIMATED_AMOUNT", "ACTUAL_AMOUNT", "PAID_AMOUNT", "NOTES", "DISPLAY_ORDER"
    );

    @Override
    public String getTableName() {
        return "Budget_Items";
    }

    @Override
    public List<String> getColumnHeaders() {
        return HEADERS;
    }

    @Override
    public List<List<Object>> fetchAllRows(Connection conn) throws SQLException {
        ResultSet rs = conn.prepareStatement(
            "SELECT ID, FUNCTION_ID, NAME, ESTIMATED_AMOUNT, ACTUAL_AMOUNT, PAID_AMOUNT, NOTES, DISPLAY_ORDER" +
            " FROM Budget_Items ORDER BY FUNCTION_ID, DISPLAY_ORDER, ID"
        ).executeQuery();
        List<List<Object>> rows = new ArrayList<>();
        while (rs.next()) {
            Object actual = rs.getObject("ACTUAL_AMOUNT");
            rows.add(Arrays.asList(
                rs.getInt("ID"),
                rs.getInt("FUNCTION_ID"),
                rs.getString("NAME"),
                rs.getInt("ESTIMATED_AMOUNT"),
                actual == null ? "" : actual,
                rs.getInt("PAID_AMOUNT"),
                rs.getString("NOTES"),
                rs.getInt("DISPLAY_ORDER")
            ));
        }
        return rows;
    }

    @Override
    public void upsertRows(Connection conn, List<List<Object>> rows) throws SQLException {
        String sql =
            "INSERT INTO Budget_Items (ID, FUNCTION_ID, NAME, ESTIMATED_AMOUNT, ACTUAL_AMOUNT, PAID_AMOUNT, NOTES, DISPLAY_ORDER)" +
            " VALUES (?, ?, ?, ?, ?, ?, ?, ?)" +
            " ON DUPLICATE KEY UPDATE FUNCTION_ID=VALUES(FUNCTION_ID), NAME=VALUES(NAME)," +
            " ESTIMATED_AMOUNT=VALUES(ESTIMATED_AMOUNT), ACTUAL_AMOUNT=VALUES(ACTUAL_AMOUNT)," +
            " PAID_AMOUNT=VALUES(PAID_AMOUNT), NOTES=VALUES(NOTES), DISPLAY_ORDER=VALUES(DISPLAY_ORDER)";
        PreparedStatement pre = conn.prepareStatement(sql);
        for (List<Object> row : rows) {
            pre.setInt(1, Integer.parseInt(String.valueOf(row.get(0))));
            pre.setInt(2, Integer.parseInt(String.valueOf(row.get(1))));
            pre.setString(3, String.valueOf(row.get(2)));
            pre.setInt(4, Integer.parseInt(String.valueOf(row.get(3))));
            String actual = String.valueOf(row.get(4)).trim();
            if (actual.isEmpty() || "null".equalsIgnoreCase(actual)) {
                pre.setNull(5, Types.INTEGER);
            } else {
                pre.setInt(5, Integer.parseInt(actual));
            }
            pre.setInt(6, Integer.parseInt(String.valueOf(row.get(5))));
            pre.setString(7, String.valueOf(row.get(6)));
            pre.setInt(8, Integer.parseInt(String.valueOf(row.get(7))));
            pre.addBatch();
        }
        pre.executeBatch();
    }
}
