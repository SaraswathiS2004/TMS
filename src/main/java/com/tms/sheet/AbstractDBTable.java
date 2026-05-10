package com.tms.sheet;

public abstract class AbstractDBTable implements DBTable {

    @Override
    public void triggerSync() {
        if (ServerMode.getCurrent() == ServerMode.Mode.READ_ONLY) {
            return;
        }
        SheetSyncManager.getInstance().queueSync(this);
    }
}
