package com.tms.data.dto;

import com.ormx.OrmX;
import com.ormx.db.query.Condition;
import com.tms.db.Invitations;

public class PeopleInvited {

    public String setData(int id, InvitedStatus invitedStatus) {
        int rowsAffected = OrmX.update(Invitations.TABLE_NAME)
            .set(Invitations.INVITED_STATUS, String.valueOf(invitedStatus))
            .where(Condition.eq(Invitations.ID, id))
            .execute();

        return rowsAffected > 0 ? "Succesfully Updated" : "Cannot Update Invited Status";
    }
}
