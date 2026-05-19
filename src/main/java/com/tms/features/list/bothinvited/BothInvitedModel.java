package com.tms.features.list.bothinvited;

import com.ormx.OrmX;
import com.ormx.db.query.Condition;
import com.tms.db.Invitations;

import java.util.List;
import java.util.Map;

class BothInvitedModel {

    public List<Map<String, Object>> displayAllPeople() {
        return OrmX.select(Invitations.TABLE_NAME)
            .where(Condition.eq(Invitations.INVITED_STATUS, "BOTH_INVITED"))
            .fetchRaw();
    }
}
