package com.tms.sheet;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public interface DBTable {
    String getTableName();
    List<String> getColumnHeaders();
    List<List<Object>> fetchAllRows(Connection conn) throws SQLException;
    void upsertRows(Connection conn, List<List<Object>> rows) throws SQLException;
    void triggerSync();
}
