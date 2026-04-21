package com.tms.features.list.notinvited;

import com.tms.data.respository.TmsDB;
import com.tms.features.list.allpeople.AllPeopleView;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

class NotInvitedModel {
    private NotInvitedView notInvitedView;

    private Connection conn;
    public NotInvitedModel(NotInvitedView notInvitedView) throws SQLException, ClassNotFoundException {
        this.notInvitedView = notInvitedView;
        conn = TmsDB.getInstance().getConnection();
    }

    public ResultSet DisplayAllPeople() throws SQLException {
        try {
            PreparedStatement pre = conn.prepareStatement("SELECT * FROM Invitations WHERE INVITED_STATUS = ?");
            pre.setString(1 , "NOT_INVITED");
            ResultSet set = pre.executeQuery();
            return set;
        }
        catch (Exception e){
            System.out.println(e);
        }
        return null;
    }
}
