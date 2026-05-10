package com.tms.sheet;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.SheetsScopes;
import com.google.api.services.sheets.v4.model.AddSheetRequest;
import com.google.api.services.sheets.v4.model.BatchUpdateSpreadsheetRequest;
import com.google.api.services.sheets.v4.model.ClearValuesRequest;
import com.google.api.services.sheets.v4.model.Request;
import com.google.api.services.sheets.v4.model.SheetProperties;
import com.google.api.services.sheets.v4.model.Spreadsheet;
import com.google.api.services.sheets.v4.model.SpreadsheetProperties;
import com.google.api.services.sheets.v4.model.ValueRange;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.ServiceAccountCredentials;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class GoogleSheetService {

    private static final GoogleSheetService INSTANCE = new GoogleSheetService();

    private Sheets sheetsService;
    private String initializedCredentialsPath;

    private GoogleSheetService() {}

    public static GoogleSheetService getInstance() {
        return INSTANCE;
    }

    public synchronized void ensureInitialized(String credentialsPath) throws Exception {
        if (sheetsService != null && credentialsPath.equals(initializedCredentialsPath)) {
            return;
        }
        try (InputStream is = new FileInputStream(credentialsPath)) {
            ServiceAccountCredentials credentials = (ServiceAccountCredentials) ServiceAccountCredentials
                .fromStream(is)
                .createScoped(Collections.singleton(SheetsScopes.SPREADSHEETS));
            sheetsService = new Sheets.Builder(
                GoogleNetHttpTransport.newTrustedTransport(),
                GsonFactory.getDefaultInstance(),
                new HttpCredentialsAdapter(credentials)
            ).setApplicationName("TMS-GoogleSheetBackup").build();
            initializedCredentialsPath = credentialsPath;
        }
    }

    public String createSpreadsheet(String title) throws IOException {
        Spreadsheet body = new Spreadsheet()
            .setProperties(new SpreadsheetProperties().setTitle(title));
        return sheetsService.spreadsheets().create(body).execute().getSpreadsheetId();
    }

    public void ensureSheetTab(String spreadsheetId, String sheetName) throws IOException {
        Spreadsheet spreadsheet = sheetsService.spreadsheets().get(spreadsheetId).execute();
        boolean exists = spreadsheet.getSheets().stream()
            .anyMatch(s -> sheetName.equals(s.getProperties().getTitle()));
        if (exists) {
            return;
        }
        AddSheetRequest addSheet = new AddSheetRequest()
            .setProperties(new SheetProperties().setTitle(sheetName));
        BatchUpdateSpreadsheetRequest batchBody = new BatchUpdateSpreadsheetRequest()
            .setRequests(Collections.singletonList(new Request().setAddSheet(addSheet)));
        sheetsService.spreadsheets().batchUpdate(spreadsheetId, batchBody).execute();
    }

    public void syncTable(String spreadsheetId, DBTable table, Connection conn) throws Exception {
        String sheetName = table.getTableName();
        ensureSheetTab(spreadsheetId, sheetName);

        List<List<Object>> allValues = new ArrayList<>();
        allValues.add(new ArrayList<>(table.getColumnHeaders()));
        allValues.addAll(table.fetchAllRows(conn));
        if(allValues.size() <= 1) {
            System.out.println("[GoogleSheetService] No data rows to sync for table " + sheetName + ". Only header will be written. Hence skipped.");
            return;
        } else {
            System.out.println("[GoogleSheetService] Syncing " + (allValues.size() - 1) + " data rows for table " + sheetName);
        }

        sheetsService.spreadsheets().values()
            .clear(spreadsheetId, sheetName, new ClearValuesRequest())
            .execute();

        ValueRange body = new ValueRange().setValues(allValues);
        sheetsService.spreadsheets().values()
            .update(spreadsheetId, sheetName + "!A1", body)
            .setValueInputOption("RAW")
            .execute();
    }

    public List<List<Object>> readTableRows(String spreadsheetId, String sheetName) throws IOException {
        System.out.println("[GoogleSheetService] readTableRows: spreadsheetId=" + spreadsheetId + ", sheetName=" + sheetName);
        ValueRange response = sheetsService.spreadsheets().values()
            .get(spreadsheetId, sheetName)
            .execute();
        List<List<Object>> values = response.getValues();
        System.out.println("[GoogleSheetService] raw values from sheet: " + (values == null ? "null" : values.size() + " rows (including header)"));
        if (values == null || values.size() <= 1) {
            System.out.println("[GoogleSheetService] No data rows found (null or header-only). Returning empty.");
            return Collections.emptyList();
        }
        System.out.println("[GoogleSheetService] Returning " + (values.size() - 1) + " data rows.");
        return values.subList(1, values.size());
    }
}
