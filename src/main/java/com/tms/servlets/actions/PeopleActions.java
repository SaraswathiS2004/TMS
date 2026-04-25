package com.tms.servlets.actions;

import com.tms.data.dto.People;
import com.tms.data.respository.TmsDB;
import com.tms.features.list.allpeople.AllPeopleView;
import com.tms.servlets.message.Message;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class PeopleActions {


    private Connection conn;
    public PeopleActions() throws SQLException, ClassNotFoundException {

        conn = TmsDB.getInstance().getConnection();
    }
    public Message addPeople(People people){
        Message message = new Message();
        try {
            message.setMessage(people.storeData());
            message.setStatus(Message.Status.SUCCESS);
        }
        catch (Exception e){
            System.out.println(e);
            message.setStatus(Message.Status.FAIL);
        }
        return message;
    }

    public ArrayList<People> listPeople(String id , String type) throws SQLException, ClassNotFoundException {

        ResultSet resultSet = null;
        if(id != null){
             resultSet = getPeopleById(id);
        }
        else if(type != null){
            resultSet = getPeopleByType(type);
        }
        else {
            resultSet = getAllPeople();
        }

        ArrayList<People> listOfPeople = new ArrayList<>();
        if(resultSet!= null) {
            int i = 0;
            while (resultSet.next()) {
                People people = new People(resultSet);
                listOfPeople.add(people);
            }
        }
        return listOfPeople;
    }

    public ResultSet getAllPeople() throws SQLException {
        try {
            PreparedStatement pre = conn.prepareStatement("SELECT * FROM Invitations");
            ResultSet set = pre.executeQuery();
            return set;
        }
        catch (Exception e){
            System.out.println(e);
        }
        return null;
    }

    public ResultSet getPeopleById(String id) throws SQLException{
        try {
            PreparedStatement pre = conn.prepareStatement("SELECT * FROM Invitations WHERE ID = ?");
            pre.setInt(1, Integer.parseInt(id));
            ResultSet set = pre.executeQuery();
            return set;
        }
        catch (Exception e){
            System.out.println(e);
        }
        return null;
    }
    public ResultSet getPeopleByType(String inviteType) throws SQLException{
        try {
            PreparedStatement pre = conn.prepareStatement("SELECT * FROM Invitations WHERE  INVITED_STATUS = ?");
            pre.setString(1 , inviteType);
            ResultSet set = pre.executeQuery();
            return set;
        }
        catch (Exception e){
            System.out.println(e);
        }
        return null;
    }
}
