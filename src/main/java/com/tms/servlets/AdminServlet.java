package com.tms.servlets;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tms.data.respository.TmsDB;
import com.tms.sheet.DBTable;
import com.tms.sheet.GoogleSheetService;
import com.tms.sheet.SheetConfig;
import com.tms.sheet.ServerMode;
import com.tms.sheet.SheetSyncManager;
import com.tms.sheet.impl.BudgetItemSplitsTable;
import com.tms.sheet.impl.BudgetItemsTable;
import com.tms.sheet.impl.FunctionsTable;
import com.tms.sheet.impl.GuestGroupsTable;
import com.tms.sheet.impl.IncomeItemsTable;
import com.tms.sheet.impl.InvitationsTable;
import com.tms.sheet.impl.PersonFunctionsTable;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.util.List;

public class AdminServlet extends HttpServlet {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String statusJson = MAPPER.writeValueAsString(SheetSyncManager.getInstance().getStatus());
        // Inject serverMode into the response JSON
        String withMode = statusJson.substring(0, statusJson.length() - 1)
            + ",\"serverMode\":\"" + ServerMode.getCurrent().name() + "\"}";
        writeJson(response, HttpServletResponse.SC_OK, withMode);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String pathInfo = request.getPathInfo();
        if ("/sheet-setup".equals(pathInfo)) {
            handleSetup(request, response);
        } else if ("/restore-from-sheet".equals(pathInfo)) {
            handleRestore(response);
        } else if ("/sync-now".equals(pathInfo)) {
            if(ServerMode.isReadOnly()) {
                writeJson(response, HttpServletResponse.SC_FORBIDDEN,
                    "{\"status\":\"FAIL\",\"message\":\"Server is in read-only mode. Sync is disabled.\"}");
                return;
            }
            SheetSyncManager.getInstance().queueSyncAll();
            writeJson(response, HttpServletResponse.SC_OK,
                "{\"status\":\"SUCCESS\",\"message\":\"Sync queued.\"}");
        } else if ("/set-mode".equals(pathInfo)) {
            handleSetMode(request, response);
        } else {
            writeJson(response, HttpServletResponse.SC_NOT_FOUND,
                "{\"status\":\"FAIL\",\"message\":\"Unknown endpoint.\"}");
        }
    }

    private void handleSetMode(HttpServletRequest request, HttpServletResponse response) throws IOException {
        JsonNode body = MAPPER.readTree(request.getReader());
        String modeStr = body.path("mode").asText();
        try {
            ServerMode.Mode mode = ServerMode.Mode.valueOf(modeStr);
            ServerMode.setCurrent(mode);
            writeJson(response, HttpServletResponse.SC_OK,
                "{\"status\":\"SUCCESS\",\"serverMode\":\"" + mode.name() + "\"}");
        } catch (IllegalArgumentException e) {
            writeJson(response, HttpServletResponse.SC_BAD_REQUEST,
                "{\"status\":\"FAIL\",\"message\":\"Invalid mode. Use READ_WRITE or READ_ONLY.\"}");
        }
    }

    private void handleSetup(HttpServletRequest request, HttpServletResponse response) throws IOException {
        JsonNode body = MAPPER.readTree(request.getReader());
        String action = body.path("action").asText();
        String credentialsFileName = body.path("credentialsFileName").asText("google-credentials.json");

        SheetConfig config = SheetConfig.getInstance();
        config.setCredentialsFileName(credentialsFileName);

        try {
            GoogleSheetService sheetService = GoogleSheetService.getInstance();
            sheetService.ensureInitialized(config.getCredentialsPath());

            String spreadsheetId;
            if ("create".equals(action)) {
                String title = body.path("title").asText("TMS Backup");
                spreadsheetId = sheetService.createSpreadsheet(title);
            } else {
                spreadsheetId = body.path("spreadsheetId").asText();
                if (spreadsheetId.isEmpty()) {
                    writeJson(response, HttpServletResponse.SC_BAD_REQUEST,
                        "{\"status\":\"FAIL\",\"message\":\"spreadsheetId is required.\"}");
                    return;
                }
            }

            config.setSpreadsheetId(spreadsheetId);
            config.save();
            SheetSyncManager.getInstance().init();

            writeJson(response, HttpServletResponse.SC_OK,
                "{\"status\":\"SUCCESS\"" +
                ",\"spreadsheetId\":\"" + spreadsheetId + "\"" +
                ",\"spreadsheetUrl\":\"https://docs.google.com/spreadsheets/d/" + spreadsheetId + "\"}");
        } catch (Exception e) {
            System.err.println("[AdminServlet] Setup error: " + e);
            writeJson(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                "{\"status\":\"FAIL\",\"message\":" + MAPPER.writeValueAsString(e.getMessage()) + "}");
        }
    }

    private void handleRestore(HttpServletResponse response) throws IOException {
        SheetConfig config = SheetConfig.getInstance();
        if (!config.isConfigured()) {
            writeJson(response, HttpServletResponse.SC_BAD_REQUEST,
                "{\"status\":\"FAIL\",\"message\":\"Google Sheet not configured.\"}");
            return;
        }
        try {
            GoogleSheetService sheetService = GoogleSheetService.getInstance();
            System.out.println("[Restore] credentialsPath=" + config.getCredentialsPath());
            System.out.println("[Restore] spreadsheetId=" + config.getSpreadsheetId());
            sheetService.ensureInitialized(config.getCredentialsPath());
            System.out.println("[Restore] GoogleSheetService initialized OK");

            // Restore in FK-safe order: Functions/Guest_Groups → Invitations → Person_Functions
            //   → Budget_Items → Budget_Item_Splits; Income_Items is independent.
            DBTable[] tables = { new FunctionsTable(), new GuestGroupsTable(),
                                 new InvitationsTable(), new PersonFunctionsTable(),
                                 new BudgetItemsTable(), new BudgetItemSplitsTable(),
                                 new IncomeItemsTable() };
            try (Connection conn = TmsDB.openConnection()) {
                System.out.println("[Restore] DB connection opened: " + conn);
                for (DBTable table : tables) {
                    System.out.println("[Restore] Reading sheet tab: " + table.getTableName());
                    List<List<Object>> rows = sheetService.readTableRows(
                        config.getSpreadsheetId(), table.getTableName());
                    System.out.println("[Restore] Rows read for " + table.getTableName() + ": " + rows.size());
                    if (!rows.isEmpty()) {
                        System.out.println("[Restore] First row sample: " + rows.get(0));
                    }
                    table.upsertRows(conn, rows);
                    System.out.println("[Restore] upsertRows done for " + table.getTableName());
                }
            }
            System.out.println("[Restore] All tables restored successfully.");
            writeJson(response, HttpServletResponse.SC_OK,
                "{\"status\":\"SUCCESS\",\"message\":\"Database repopulated from sheet.\"}");
        } catch (Exception e) {
            System.err.println("[AdminServlet] Restore error: " + e);
            e.printStackTrace();
            writeJson(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                "{\"status\":\"FAIL\",\"message\":" + MAPPER.writeValueAsString(e.getMessage()) + "}");
        }
    }

    private void writeJson(HttpServletResponse response, int status, String json) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();
        out.print(json);
        out.flush();
    }
}
