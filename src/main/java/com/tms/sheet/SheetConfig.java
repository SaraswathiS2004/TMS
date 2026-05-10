package com.tms.sheet;

import com.configwatch.ConfigRegistry;
import com.configwatch.ConfigStore;

import java.io.File;

public class SheetConfig {

    private static final String KEY_SPREADSHEET_ID    = "spreadsheetId";
    private static final String KEY_CREDENTIALS_FILE  = "credentialsFileName";
    private static final String DEFAULT_CREDENTIALS   = "google-credentials.json";

    private static final SheetConfig INSTANCE = new SheetConfig();

    // Set by AppContextListener so credentials file can be resolved relative to WEB-INF/classes
    private static String classesDir;

    private SheetConfig() {}

    public static SheetConfig getInstance() {
        return INSTANCE;
    }

    /** Called by AppContextListener to provide the WEB-INF/classes path for credential file resolution. */
    public static void setClassesDir(String dir) {
        classesDir = dir;
    }

    private static ConfigStore store() {
        return ConfigRegistry.get("sheet");
    }

    public boolean isConfigured() {
        String id = store().get(KEY_SPREADSHEET_ID);
        return id != null && !id.isEmpty();
    }

    public String getSpreadsheetId() {
        return store().get(KEY_SPREADSHEET_ID);
    }

    public void setSpreadsheetId(String id) {
        store().set(KEY_SPREADSHEET_ID, id);
    }

    public String getCredentialsFileName() {
        return store().get(KEY_CREDENTIALS_FILE, DEFAULT_CREDENTIALS);
    }

    public void setCredentialsFileName(String name) {
        store().set(KEY_CREDENTIALS_FILE, name);
    }

    public String getCredentialsPath() {
        String dir = classesDir != null ? classesDir : System.getProperty("user.home");
        return new File(dir, getCredentialsFileName()).getAbsolutePath();
    }

    /** No-op — configM persists on every set(). Kept for call-site compatibility. */
    public void save() {}
}
