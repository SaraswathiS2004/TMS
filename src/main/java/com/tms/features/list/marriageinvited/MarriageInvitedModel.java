package com.tms.features.list.marriageinvited;

import com.ormx.OrmX;
import com.ormx.db.query.Condition;
import com.tms.db.InvitationsTable;

import java.util.List;
import java.util.Map;

class MarriageInvitedModel {

    public List<Map<String, Object>> displayAllPeople() {
        return OrmX.select(InvitationsTable.TABLE_NAME)
            .where(Condition.eq(InvitationsTable.INVITED_STATUS, "MARRIAGE_INVITED"))
            .fetchRaw();
    }
}
