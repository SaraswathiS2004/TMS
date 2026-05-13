package com.tms.features.MarkAsInvited;

import com.tms.data.dto.InvitedStatus;
import com.tms.data.dto.PeopleInvited;

class MarkAsInvitedModel {

    private final MarkAsInvitedView markAsInvitedView;

    public MarkAsInvitedModel(MarkAsInvitedView markAsInvitedView) {
        this.markAsInvitedView = markAsInvitedView;
    }

    public void setData(int id, InvitedStatus invitedStatus) {
        PeopleInvited peopleInvited = new PeopleInvited();
        String message = peopleInvited.setData(id, invitedStatus);
        markAsInvitedView.showMessage(message);
    }
}
