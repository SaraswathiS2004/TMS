package com.tms.features.peopleAdded;

import com.tms.data.dto.InvitedStatus;
import com.tms.data.dto.People;
import com.tms.data.dto.RelationType;

import java.sql.SQLException;
import java.util.Scanner;

public class PeopleAddedModel {

    private PeoplesAddedView peoplesAddedView;

    public PeopleAddedModel(){}
    public PeopleAddedModel(PeoplesAddedView peoplesAddedView){
        this.peoplesAddedView = peoplesAddedView;
    }
    public void setData(String name , String city , int numberOfCount , RelationType type) throws SQLException, ClassNotFoundException {
        People people = new People();
        people.setName(name);
        people.setCity(city);
        people.setNumberOfPerson(numberOfCount);
        people.setRelationType(type);
        people.setInvitedStatus(InvitedStatus.NOT_INVITED);
        setData(people);
    }

    public void setData(People people) throws SQLException {
        String result = people.storeData();
        successfullMessage(result);
    }

    public void successfullMessage(String Message){

        peoplesAddedView.showMessage(Message);

    }

}
