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

public class IncomeItemsTable extends AbstractDBTable {

    private static final List<String> HEADERS = List.of(
        "ID", "SOURCE", "INCOME_DATE", "ESTIMATED_AMOUNT", "ACTUAL_AMOUNT", "NOTES", "DISPLAY_ORDER"
    );

    @Override
    public String getTableName() {
        return "Income_Items";
    }

    @Override
    public List<String> getColumnHeaders() {
        return HEADERS;
    }

    @Override
    public List<List<Object>> fetchAllRows(Connection conn) throws SQLException {
        ResultSet rs = conn.prepareStatement(
            "SELECT ID, SOURCE, INCOME_DATE, ESTIMATED_AMOUNT, ACTUAL_AMOUNT, NOTES, DISPLAY_ORDER" +
            " FROM Income_Items ORDER BY DISPLAY_ORDER, ID"
        ).executeQuery();
        List<List<Object>> rows = new ArrayList<>();
        while (rs.next()) {
            String date = rs.getString("INCOME_DATE");
            Object actual = rs.getObject("ACTUAL_AMOUNT");
            rows.add(Arrays.asList(
                rs.getInt("ID"),
                rs.getString("SOURCE"),
                date == null ? "" : date,
                rs.getInt("ESTIMATED_AMOUNT"),
                actual == null ? "" : actual,
                rs.getString("NOTES"),
                rs.getInt("DISPLAY_ORDER")
            ));
        }
        return rows;
    }

    @Override
    public void upsertRows(Connection conn, List<List<Object>> rows) throws SQLException {
        String sql =
            "INSERT INTO Income_Items (ID, SOURCE, INCOME_DATE, ESTIMATED_AMOUNT, ACTUAL_AMOUNT, NOTES, DISPLAY_ORDER)" +
            " VALUES (?, ?, ?, ?, ?, ?, ?)" +
            " ON DUPLICATE KEY UPDATE SOURCE=VALUES(SOURCE), INCOME_DATE=VALUES(INCOME_DATE)," +
            " ESTIMATED_AMOUNT=VALUES(ESTIMATED_AMOUNT), ACTUAL_AMOUNT=VALUES(ACTUAL_AMOUNT)," +
            " NOTES=VALUES(NOTES), DISPLAY_ORDER=VALUES(DISPLAY_ORDER)";
        PreparedStatement pre = conn.prepareStatement(sql);
        for (List<Object> row : rows) {
            pre.setInt(1, Integer.parseInt(String.valueOf(row.get(0))));
            pre.setString(2, String.valueOf(row.get(1)));
            String date = String.valueOf(row.get(2)).trim();
            if (date.isEmpty() || "null".equalsIgnoreCase(date)) {
                pre.setNull(3, Types.VARCHAR);
            } else {
                pre.setString(3, date);
            }
            pre.setInt(4, Integer.parseInt(String.valueOf(row.get(3))));
            String actual = String.valueOf(row.get(4)).trim();
            if (actual.isEmpty() || "null".equalsIgnoreCase(actual)) {
                pre.setNull(5, Types.INTEGER);
            } else {
                pre.setInt(5, Integer.parseInt(actual));
            }
            pre.setString(6, String.valueOf(row.get(5)));
            pre.setInt(7, Integer.parseInt(String.valueOf(row.get(6))));
            pre.addBatch();
        }
        pre.executeBatch();
    }
}
