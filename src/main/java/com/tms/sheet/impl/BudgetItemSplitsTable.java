package com.tms.sheet.impl;

import com.tms.sheet.AbstractDBTable;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class BudgetItemSplitsTable extends AbstractDBTable {

    private static final List<String> HEADERS = List.of("ID", "BUDGET_ITEM_ID", "LABEL", "AMOUNT", "DISPLAY_ORDER");

    @Override
    public String getTableName() {
        return "Budget_Item_Splits";
    }

    @Override
    public List<String> getColumnHeaders() {
        return HEADERS;
    }

    @Override
    public List<List<Object>> fetchAllRows(Connection conn) throws SQLException {
        ResultSet rs = conn.prepareStatement(
            "SELECT ID, BUDGET_ITEM_ID, LABEL, AMOUNT, DISPLAY_ORDER FROM Budget_Item_Splits ORDER BY BUDGET_ITEM_ID, DISPLAY_ORDER, ID"
        ).executeQuery();
        List<List<Object>> rows = new ArrayList<>();
        while (rs.next()) {
            rows.add(Arrays.asList(
                rs.getInt("ID"),
                rs.getInt("BUDGET_ITEM_ID"),
                rs.getString("LABEL"),
                rs.getInt("AMOUNT"),
                rs.getInt("DISPLAY_ORDER")
            ));
        }
        return rows;
    }

    @Override
    public void upsertRows(Connection conn, List<List<Object>> rows) throws SQLException {
        String sql =
            "INSERT INTO Budget_Item_Splits (ID, BUDGET_ITEM_ID, LABEL, AMOUNT, DISPLAY_ORDER) VALUES (?, ?, ?, ?, ?)" +
            " ON DUPLICATE KEY UPDATE BUDGET_ITEM_ID=VALUES(BUDGET_ITEM_ID), LABEL=VALUES(LABEL)," +
            " AMOUNT=VALUES(AMOUNT), DISPLAY_ORDER=VALUES(DISPLAY_ORDER)";
        PreparedStatement pre = conn.prepareStatement(sql);
        for (List<Object> row : rows) {
            pre.setInt(1, Integer.parseInt(String.valueOf(row.get(0))));
            pre.setInt(2, Integer.parseInt(String.valueOf(row.get(1))));
            pre.setString(3, String.valueOf(row.get(2)));
            pre.setInt(4, Integer.parseInt(String.valueOf(row.get(3))));
            pre.setInt(5, Integer.parseInt(String.valueOf(row.get(4))));
            pre.addBatch();
        }
        pre.executeBatch();
    }
}
