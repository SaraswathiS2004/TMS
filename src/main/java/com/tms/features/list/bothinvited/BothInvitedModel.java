package com.tms.features.list.bothinvited;

import com.tms.data.respository.TmsDB;
import com.tms.features.list.engagementinvited.EngagementInvitedView;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

class BothInvitedModel {

    private BothInvitedView bothInvitedView;

    private Connection conn;
    public BothInvitedModel(BothInvitedView bothInvitedView) throws SQLException, ClassNotFoundException {
        this.bothInvitedView = bothInvitedView;
        conn = TmsDB.getInstance().getConnection();
    }

    public ResultSet DisplayAllPeople() throws SQLException {
        try {
            PreparedStatement pre = conn.prepareStatement("SELECT * FROM Invitations WHERE INVITED_STATUS = ?");
            pre.setString(1 , "BOTH_INVITED");
            ResultSet set = pre.executeQuery();
            return set;
        }
        catch (Exception e){
            System.out.println(e);
        }
        return null;
    }
}
