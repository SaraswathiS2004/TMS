package com.tms.sheet;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;

public class SheetConfig {

    private static final String CONFIG_FILE = "sheet-config.properties";

    private static SheetConfig instance;
    private static String classesDir;

    private String spreadsheetId;
    private String credentialsFileName = "google-credentials.json";

    private SheetConfig() {
        load();
    }

    public static void init(String classesDirPath) {
        classesDir = classesDirPath;
        instance = new SheetConfig();
    }

    public static SheetConfig getInstance() {
        if (instance == null) {
            instance = new SheetConfig();
        }
        return instance;
    }

    private void load() {
        File configFile = getConfigFile();
        if (!configFile.exists()) {
            return;
        }
        try (InputStream is = new FileInputStream(configFile)) {
            Properties props = new Properties();
            props.load(is);
            spreadsheetId = props.getProperty("spreadsheetId");
            credentialsFileName = props.getProperty("credentialsFileName", "google-credentials.json");
        } catch (IOException e) {
            System.err.println("[SheetConfig] Failed to load: " + e.getMessage());
        }
    }

    public void save() {
        Properties props = new Properties();
        if (spreadsheetId != null) {
            props.setProperty("spreadsheetId", spreadsheetId);
        }
        props.setProperty("credentialsFileName", credentialsFileName);
        try (OutputStream os = new FileOutputStream(getConfigFile())) {
            props.store(os, "TMS Google Sheet Config");
        } catch (IOException e) {
            System.err.println("[SheetConfig] Failed to save: " + e.getMessage());
        }
    }

    public boolean isConfigured() {
        return spreadsheetId != null && !spreadsheetId.isEmpty();
    }

    public String getCredentialsPath() {
        String dir = classesDir != null ? classesDir : System.getProperty("user.home");
        return new File(dir, credentialsFileName).getAbsolutePath();
    }

    private File getConfigFile() {
        String dir = classesDir != null ? classesDir : System.getProperty("user.home");
        return new File(dir, CONFIG_FILE);
    }

    public String getSpreadsheetId() { return spreadsheetId; }
    public void setSpreadsheetId(String spreadsheetId) { this.spreadsheetId = spreadsheetId; }
    public String getCredentialsFileName() { return credentialsFileName; }
    public void setCredentialsFileName(String credentialsFileName) { this.credentialsFileName = credentialsFileName; }
}
