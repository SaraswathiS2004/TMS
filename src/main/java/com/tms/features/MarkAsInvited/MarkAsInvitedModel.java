package com.tms.features.MarkAsInvited;

import com.tms.data.dto.InvitedStatus;
import com.tms.data.dto.PeopleInvited;

import java.sql.SQLException;

class MarkAsInvitedModel {

    private MarkAsInvitedView markAsInvitedView;
    public MarkAsInvitedModel(MarkAsInvitedView markAsInvitedView){
        this.markAsInvitedView = markAsInvitedView;
    }

    public void setData(int id , InvitedStatus invitedStatus) throws SQLException, ClassNotFoundException {
        PeopleInvited peopleInvited = new PeopleInvited();
        String message = peopleInvited.setData(id , invitedStatus);
        markAsInvitedView.showMessage(message);
    }
}
