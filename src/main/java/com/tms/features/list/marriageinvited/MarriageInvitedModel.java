package com.tms.features.list.marriageinvited;

import com.ormx.OrmX;
import com.ormx.db.query.Condition;
import com.tms.db.Invitations;

import java.util.List;
import java.util.Map;

class MarriageInvitedModel {

    public List<Map<String, Object>> displayAllPeople() {
        return OrmX.select(Invitations.TABLE_NAME)
            .where(Condition.eq(Invitations.INVITED_STATUS, "MARRIAGE_INVITED"))
            .fetchRaw();
    }
}
