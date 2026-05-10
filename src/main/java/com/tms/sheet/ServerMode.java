package com.tms.sheet;

import com.configwatch.ConfigRegistry;

public class ServerMode {

    public enum Mode { READ_WRITE, READ_ONLY }

    private static final String KEY = "serverMode";

    private ServerMode() {}

    public static Mode getCurrent() {
        String value = ConfigRegistry.get("sheet").get(KEY, Mode.READ_WRITE.name());
        try {
            return Mode.valueOf(value);
        } catch (IllegalArgumentException e) {
            return Mode.READ_WRITE;
        }
    }

    public static boolean isReadOnly() {
       return getCurrent() == Mode.READ_ONLY;
    }

    public static void setCurrent(Mode mode) {
        ConfigRegistry.get("sheet").set(KEY, mode.name());
    }
}
