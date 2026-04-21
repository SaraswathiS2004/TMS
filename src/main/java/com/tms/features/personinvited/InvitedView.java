package com.tms.features.personinvited;

import com.tms.data.dto.InvitedStatus;
import com.tms.data.dto.RelationType;
import com.tms.features.input.Input;

import java.sql.SQLException;
import java.util.Scanner;

public class InvitedView {

    private InvitedModel invitedModel;

    public InvitedView(){
        this.invitedModel = new InvitedModel(this);
    }

    public void init() throws SQLException, ClassNotFoundException {
        Scanner scan = Input.getInstance();
        System.out.println("Enter the Id Of People");
        int id = scan.nextInt();
        System.out.println("Enter The Type Of Invited");
        System.out.println("Example : MARRIAGE_INVITED , ENGAGEMENT_INVITED , BOTH_INVITED");
        InvitedStatus invitedStatus = InvitedStatus.valueOf(scan.next().toUpperCase());

        invitedModel.setData(id , invitedStatus);
    }

    public void showMessage(String message){
        System.out.println(message);
    }
}
