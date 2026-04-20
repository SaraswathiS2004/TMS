package com.tms.features.peopleAdded;

import com.tms.data.dto.InvitedStatus;
import com.tms.data.dto.People;
import com.tms.data.dto.RelationType;

import java.sql.SQLException;
import java.util.Scanner;

public class PeoplesAddedView {
    private PeopleAddedModel peopleAddedModel;
    public PeoplesAddedView(){
        this.peopleAddedModel = new PeopleAddedModel(this);
    }
    public void init() throws SQLException, ClassNotFoundException {
        Scanner scan = new Scanner(System.in);
        System.out.println("Enter the Name");
        String name = scan.next();
        System.out.println("Enter the City");
        String city = scan.next();
        System.out.println("Enter the Number of People will Come");
        int numberOfCount = scan.nextInt();
        System.out.println("Enter the Relation type : ");
        System.out.println("Example :");
        System.out.println("CLOSE , DISTANCE , FRIENDS");
        RelationType type = RelationType.valueOf(scan.next().toUpperCase());

        peopleAddedModel.setData(name , city , numberOfCount , type);

    }
    public void showMessage(String message){
        System.out.println(message);
    }
}
