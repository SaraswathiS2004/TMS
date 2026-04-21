package com.tms.features.personinvited;

import com.tms.data.dto.InvitedStatus;
import com.tms.data.dto.PeopleInvited;
import com.tms.data.dto.RelationType;

import java.sql.SQLException;

class InvitedModel {

    private InvitedView invitedView;
    public InvitedModel(InvitedView invitedView){
        this.invitedView = invitedView;
    }

    public void setData(int id , InvitedStatus invitedStatus) throws SQLException, ClassNotFoundException {
        PeopleInvited peopleInvited = new PeopleInvited();
        String message = peopleInvited.setData(id , invitedStatus);
        invitedView.showMessage(message);
    }
}
