package com.tms.features.list.allpeople;

import com.ormx.OrmX;
import com.ormx.db.query.Condition;
import com.tms.db.InvitationsTable;

import java.util.List;
import java.util.Map;

class AllPeopleModel {

    public List<Map<String, Object>> displayAllPeople() {
        return OrmX.select(InvitationsTable.TABLE_NAME).fetchRaw();
    }

    public List<Map<String, Object>> getPeopleById(String id) {
        return OrmX.select(InvitationsTable.TABLE_NAME)
            .where(Condition.eq(InvitationsTable.ID, Integer.parseInt(id)))
            .fetchRaw();
    }

    public List<Map<String, Object>> getPeopleByType(String inviteType) {
        return OrmX.select(InvitationsTable.TABLE_NAME)
            .where(Condition.eq(InvitationsTable.INVITED_STATUS, inviteType))
            .fetchRaw();
    }
}
