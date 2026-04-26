package com.tms.data.dto;

import com.tms.data.respository.TmsDB;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class People {

    private String name;
    private int numberOfPerson;
    private RelationType relationType;
    private InvitedStatus invitedStatus = InvitedStatus.NOT_INVITED;
    private String city;
    private int id;
    private List<Integer> invitedFunctionIds = new ArrayList<>();

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

    // storeData kept for CLI backward compatibility
    public String storeData() throws SQLException {
        try {
            PreparedStatement pre = conn.prepareStatement(
                "INSERT INTO Invitations (NAME, CITY, RELATION_TYPE, NUMBER_OF_PEOPLE_WILL_COME, INVITED_STATUS) " +
                "VALUES (?, ?, ?, ?, ?)"
            );
            pre.setString(1, name);
            pre.setString(2, city);
            pre.setString(3, relationType.toString());
            pre.setInt(4, numberOfPerson);
            pre.setString(5, invitedStatus.toString());
            int rowsAffected = pre.executeUpdate();
            return rowsAffected > 0 ? "Successfully Added!" : "Cannot add person";
        } catch (Exception e) {
            System.out.println(e);
        }
        return "";
    }

    public InvitedStatus getInvitedStatus() { return invitedStatus; }
    public void setInvitedStatus(InvitedStatus invitedStatus) { this.invitedStatus = invitedStatus; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getNumberOfPerson() { return numberOfPerson; }
    public void setNumberOfPerson(int numberOfPerson) { this.numberOfPerson = numberOfPerson; }

    public RelationType getRelationType() { return relationType; }
    public void setRelationType(RelationType relationType) { this.relationType = relationType; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public List<Integer> getInvitedFunctionIds() { return invitedFunctionIds; }
    public void setInvitedFunctionIds(List<Integer> invitedFunctionIds) {
        this.invitedFunctionIds = invitedFunctionIds != null ? invitedFunctionIds : new ArrayList<>();
    }
}
