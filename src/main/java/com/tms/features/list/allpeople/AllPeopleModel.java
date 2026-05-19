package com.tms.features.list.allpeople;

import com.ormx.OrmX;
import com.ormx.db.query.Condition;
import com.tms.db.Invitations;

import java.util.List;
import java.util.Map;

class AllPeopleModel {

    public List<Map<String, Object>> displayAllPeople() {
        return OrmX.select(Invitations.TABLE_NAME).fetchRaw();
    }

    public List<Map<String, Object>> getPeopleById(String id) {
        return OrmX.select(Invitations.TABLE_NAME)
            .where(Condition.eq(Invitations.ID, Integer.parseInt(id)))
            .fetchRaw();
    }

    public List<Map<String, Object>> getPeopleByType(String inviteType) {
        return OrmX.select(Invitations.TABLE_NAME)
            .where(Condition.eq(Invitations.INVITED_STATUS, inviteType))
            .fetchRaw();
    }
}
