package com.tms.features.list.marriageinvited;

import com.tms.data.respository.TmsDB;
import com.tms.features.list.notinvited.NotInvitedView;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

class MarriageInvitedModel {
    private MarriageInvitedView marriageInvitedView;

    private Connection conn;
    public MarriageInvitedModel(MarriageInvitedView marriageInvitedView) throws SQLException, ClassNotFoundException {
        this.marriageInvitedView = marriageInvitedView;
        conn = TmsDB.getInstance().getConnection();
    }

    public ResultSet DisplayAllPeople() throws SQLException {
        try {
            PreparedStatement pre = conn.prepareStatement("SELECT * FROM Invitations WHERE INVITED_STATUS = ?");
            pre.setString(1 , "MARRIAGE_INVITED");
            ResultSet set = pre.executeQuery();
            return set;
        }
        catch (Exception e){
            System.out.println(e);
        }
        return null;
    }
}
