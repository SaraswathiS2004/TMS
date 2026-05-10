package com.tms.sheet;

import java.util.LinkedHashMap;
import java.util.Map;

public class SheetSyncStatus {

    public static class TableSyncInfo {
        public boolean success;
        public String error;
        public long timestamp;

        public TableSyncInfo(boolean success, String error, long timestamp) {
            this.success = success;
            this.error = error;
            this.timestamp = timestamp;
        }
    }

    private boolean configured;
    private String spreadsheetId;
    private String spreadsheetUrl;
    private boolean lastSyncSuccess = true;
    private String lastSyncError;
    private long lastSyncTimestamp;
    private Map<String, TableSyncInfo> tableStatuses = new LinkedHashMap<>();

    public boolean isConfigured() { return configured; }
    public void setConfigured(boolean configured) { this.configured = configured; }

    public String getSpreadsheetId() { return spreadsheetId; }
    public void setSpreadsheetId(String spreadsheetId) { this.spreadsheetId = spreadsheetId; }

    public String getSpreadsheetUrl() { return spreadsheetUrl; }
    public void setSpreadsheetUrl(String spreadsheetUrl) { this.spreadsheetUrl = spreadsheetUrl; }

    public boolean isLastSyncSuccess() { return lastSyncSuccess; }
    public void setLastSyncSuccess(boolean lastSyncSuccess) { this.lastSyncSuccess = lastSyncSuccess; }

    public String getLastSyncError() { return lastSyncError; }
    public void setLastSyncError(String lastSyncError) { this.lastSyncError = lastSyncError; }

    public long getLastSyncTimestamp() { return lastSyncTimestamp; }
    public void setLastSyncTimestamp(long lastSyncTimestamp) { this.lastSyncTimestamp = lastSyncTimestamp; }

    public Map<String, TableSyncInfo> getTableStatuses() { return tableStatuses; }
    public void setTableStatuses(Map<String, TableSyncInfo> tableStatuses) { this.tableStatuses = tableStatuses; }
}
