package com.tms.sheet;

import com.tms.data.respository.TmsDB;
import com.tms.sheet.impl.FunctionsTable;
import com.tms.sheet.impl.InvitationPersonFunctionsTable;
import com.tms.sheet.impl.InvitationPersonsTable;
import com.tms.sheet.impl.InvitationsTable;
import com.tms.sheet.impl.PersonFunctionsTable;

import java.sql.Connection;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class SheetSyncManager {

    private static final SheetSyncManager INSTANCE = new SheetSyncManager();

    private final ExecutorService executor = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "SheetSyncThread");
        t.setDaemon(true);
        return t;
    });

    private final SheetSyncStatus status = new SheetSyncStatus();

    private SheetSyncManager() {}

    public static SheetSyncManager getInstance() {
        return INSTANCE;
    }

    public void init() {
        SheetConfig config = SheetConfig.getInstance();
        status.setConfigured(config.isConfigured());
        if (config.isConfigured()) {
            status.setSpreadsheetId(config.getSpreadsheetId());
            status.setSpreadsheetUrl("https://docs.google.com/spreadsheets/d/" + config.getSpreadsheetId());
        }
    }

    public void queueSync(DBTable table) {
        if(ServerMode.isReadOnly()) {
            System.out.println("[SheetSync] Server is in read-only mode, skipping sync for " + table.getTableName());
            return;
        }
        SheetConfig config = SheetConfig.getInstance();
        if (!config.isConfigured()) {
            return;
        }
        executor.submit(() -> doSync(table, config));
    }

    public void queueSyncAll() {
        queueSync(new FunctionsTable());
        queueSync(new InvitationsTable());
        queueSync(new PersonFunctionsTable());
        queueSync(new InvitationPersonsTable());
        queueSync(new InvitationPersonFunctionsTable());
    }

    public SheetSyncStatus getStatus() {
        SheetConfig config = SheetConfig.getInstance();
        status.setConfigured(config.isConfigured());
        if (config.isConfigured()) {
            status.setSpreadsheetId(config.getSpreadsheetId());
            status.setSpreadsheetUrl("https://docs.google.com/spreadsheets/d/" + config.getSpreadsheetId());
        }
        return status;
    }

    public void shutdown() {
        executor.shutdown();
        try {
            executor.awaitTermination(10, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private void doSync(DBTable table, SheetConfig config) {
        try (Connection conn = TmsDB.openConnection()) {
            GoogleSheetService.getInstance().ensureInitialized(config.getCredentialsPath());
            GoogleSheetService.getInstance().syncTable(config.getSpreadsheetId(), table, conn);
            recordTableStatus(table.getTableName(), true, null);
        } catch (Exception e) {
            System.err.println("[SheetSync] Failed to sync " + table.getTableName() + ": " + e.getMessage());
            recordTableStatus(table.getTableName(), false, e.getMessage());
        }
    }

    private synchronized void recordTableStatus(String tableName, boolean success, String error) {
        status.getTableStatuses().put(
            tableName,
            new SheetSyncStatus.TableSyncInfo(success, error, System.currentTimeMillis())
        );
        status.setLastSyncTimestamp(System.currentTimeMillis());
        if (!success) {
            status.setLastSyncSuccess(false);
            status.setLastSyncError(error);
        } else {
            boolean allSuccessful = status.getTableStatuses().values().stream()
                .allMatch(info -> info.success);
            status.setLastSyncSuccess(allSuccessful);
            if (allSuccessful) {
                status.setLastSyncError(null);
            }
        }
    }
}
