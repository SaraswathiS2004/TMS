package com.tms.data.dto;

import com.tms.data.respository.TmsDB;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class People {
    private String name;
    private int numberOfPerson;
    private RelationType relationType;
    private  InvitedStatus invitedStatus;
    private String city;

    private Connection conn;

    public People() throws SQLException, ClassNotFoundException {
        conn = TmsDB.getInstance().getConnection();
    }


    public InvitedStatus getInvitedStatus() {
        return invitedStatus;
    }

    public void setInvitedStatus(InvitedStatus invitedStatus) {
        this.invitedStatus = InvitedStatus.NOT_INVITED;
    }

    public String getNames() {
        return name;
    }

    public void setNames(String name) {
        this.name = name;
    }

    public int getNumberOfPeople() {
        return numberOfPerson;
    }

    public void setNumberOfPeople(int numberOfPeople) {
        this.numberOfPerson = numberOfPeople;
    }

    public RelationType getType() {
        return relationType;
    }

    public void setType(RelationType relationType) {
        this.relationType = relationType;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }


    public String storeData() throws SQLException{
        try {
            PreparedStatement pre = conn.prepareStatement("INSERT INTO Invitations  (NAME , CITY ,RELATION_TYPE ,  NUMBER_OF_PEOPLE_WILL_COME , INVITED_STATUS ) values ( ? , ? , ? ,? , ?)");
            pre.setString(1 , name);
            pre.setString(2 , city);
            pre.setString(3 , relationType.toString());
            pre.setInt(4 , numberOfPerson);
            pre.setString(5 , invitedStatus.toString());
            int rowsAffected = pre.executeUpdate();

            if(rowsAffected > 0){

                return "Successfully Added!";
            }
            else{
               return "Do not added";
            }
        }
        catch (Exception e){
            System.out.println(e);
        }
        return "";

    }

}
