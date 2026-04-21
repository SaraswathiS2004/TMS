package com.tms.data.dto;

import com.tms.data.respository.TmsDB;
import com.tms.features.input.Input;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class PeopleInvited {
    private Connection conn;

    public PeopleInvited() throws SQLException, ClassNotFoundException {
        conn = TmsDB.getInstance().getConnection();
    }
    public String setData(int id , InvitedStatus invitedStatus) throws SQLException {
        PreparedStatement pre = conn.prepareStatement("UPDATE Invitations SET INVITED_STATUS = (?) WHERE ID = (?)");
        pre.setString(1 , String.valueOf(invitedStatus));
        pre.setInt(2 , id);
        int rowsAffected = pre.executeUpdate();

        if(rowsAffected > 0){
           return "Succesfully Updated";
        }
        else {
          return "Cannot Update Invited Status";
        }
    }
}
