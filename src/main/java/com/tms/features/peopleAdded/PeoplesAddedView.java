package com.tms.features.peopleAdded;

import com.tms.data.dto.RelationType;
import com.tms.features.input.Input;

import java.util.Scanner;

public class PeoplesAddedView {

    private final PeopleAddedModel peopleAddedModel = new PeopleAddedModel(this);

    public void init() {
        Scanner scan = Input.getInstance();
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
        peopleAddedModel.setData(name, city, numberOfCount, type);
    }

    public void showMessage(String message) {
        System.out.println(message);
    }
}
