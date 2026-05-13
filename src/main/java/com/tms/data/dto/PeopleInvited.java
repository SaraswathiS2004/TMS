package com.tms.data.dto;

import com.ormx.OrmX;
import com.ormx.db.query.Condition;
import com.tms.db.InvitationsTable;

public class PeopleInvited {

    public String setData(int id, InvitedStatus invitedStatus) {
        int rowsAffected = OrmX.update(InvitationsTable.TABLE_NAME)
            .set(InvitationsTable.INVITED_STATUS, String.valueOf(invitedStatus))
            .where(Condition.eq(InvitationsTable.ID, id))
            .execute();

        return rowsAffected > 0 ? "Succesfully Updated" : "Cannot Update Invited Status";
    }
}
