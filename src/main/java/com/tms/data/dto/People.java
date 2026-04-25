package com.tms.data.dto;

import com.tms.data.respository.TmsDB;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class People {
    private String name;
    private int numberOfPerson;
    private RelationType relationType;
    private InvitedStatus invitedStatus = InvitedStatus.NOT_INVITED;

    private String city;
    private int id;

    private Connection conn;

    public People() throws SQLException, ClassNotFoundException {
        conn = TmsDB.getInstance().getConnection();
    }
    public People(ResultSet resultSet) throws SQLException, ClassNotFoundException {
        this();
        setId(resultSet.getInt("ID"));
        setName(resultSet.getString("NAME"));
        setCity(resultSet.getString("CITY"));
        setNumberOfPerson(resultSet.getInt("NUMBER_OF_PEOPLE_WILL_COME"));
        setInvitedStatus(InvitedStatus.valueOf(resultSet.getString("INVITED_STATUS")));
        setRelationType(RelationType.valueOf(resultSet.getString("RELATION_TYPE")));
    }


    public InvitedStatus getInvitedStatus() {
        return invitedStatus;
    }

    public void setInvitedStatus(InvitedStatus invitedStatus) {
        this.invitedStatus = invitedStatus;
    }

    public String getName() {
        return name;
    }
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getNumberOfPerson() {
        return numberOfPerson;
    }

    public void setNumberOfPerson(int numberOfPeople) {
        this.numberOfPerson = numberOfPeople;
    }

    public RelationType getRelationType() {
        return relationType;
    }

    public void setRelationType(RelationType relationType) {
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
               return "cannot add people";
            }
        }
        catch (Exception e){
            System.out.println(e);
        }
        return "";

    }

}
