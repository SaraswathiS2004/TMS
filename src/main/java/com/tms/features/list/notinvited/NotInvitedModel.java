package com.tms.features.list.notinvited;

import com.ormx.OrmX;
import com.ormx.db.query.Condition;
import com.tms.db.Invitations;

import java.util.List;
import java.util.Map;

class NotInvitedModel {

    public List<Map<String, Object>> displayAllPeople() {
        return OrmX.select(Invitations.TABLE_NAME)
            .where(Condition.eq(Invitations.INVITED_STATUS, "NOT_INVITED"))
            .fetchRaw();
    }
}
