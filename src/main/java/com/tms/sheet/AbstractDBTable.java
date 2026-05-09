package com.tms.sheet;

public abstract class AbstractDBTable implements DBTable {

    @Override
    public void triggerSync() {
        SheetSyncManager.getInstance().queueSync(this);
    }
}
