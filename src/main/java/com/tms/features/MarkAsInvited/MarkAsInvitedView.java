package com.tms.features.MarkAsInvited;

import com.tms.data.dto.InvitedStatus;
import com.tms.features.input.Input;

import java.util.Scanner;

public class MarkAsInvitedView {

    private final MarkAsInvitedModel invitedModel = new MarkAsInvitedModel(this);

    public void init() {
        Scanner scan = Input.getInstance();
        System.out.println("Enter the Id Of People");
        int id = scan.nextInt();
        System.out.println("Enter The Type Of Invited");
        System.out.println("Example : MARRIAGE_INVITED , ENGAGEMENT_INVITED , BOTH_INVITED");
        InvitedStatus invitedStatus = InvitedStatus.valueOf(scan.next().toUpperCase());
        invitedModel.setData(id, invitedStatus);
    }

    public void showMessage(String message) {
        System.out.println(message);
    }
}
