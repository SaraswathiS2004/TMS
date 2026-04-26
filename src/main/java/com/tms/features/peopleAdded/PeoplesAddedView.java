package com.tms.features.peopleAdded;

import com.tms.data.dto.InvitedStatus;
import com.tms.data.dto.People;
import com.tms.data.dto.RelationType;
import com.tms.features.input.Input;

import java.sql.SQLException;
import java.util.Scanner;

public class PeoplesAddedView {
    private PeopleAddedModel peopleAddedModel;
    public PeoplesAddedView(){
        this.peopleAddedModel = new PeopleAddedModel(this);
    }
    public void init() throws SQLException, ClassNotFoundException {
        Scanner scan = Input.getInstance();
        System.out.println("Enter the Name");
        String name = scan.next();
        System.out.println("Enter the City");
        String city = scan.next();
        System.out.println("Enter the Number of People will Come");
        int numberOfCount = scan.nextInt();
        System.out.println("Enter the Relation type : ");
        System.out.println("Example :");
        System.out.println("CLOSE_RELATIVE , DISTANCE_RELATIVE , FRIENDS;");
        RelationType type = RelationType.valueOf(scan.next().toUpperCase());

        peopleAddedModel.setData(name , city , numberOfCount , type);

    }
    public void showMessage(String message){
        System.out.println(message);
    }
}
