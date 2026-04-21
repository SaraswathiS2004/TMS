package com.tms.features.list.engagementinvited;

import com.tms.data.respository.TmsDB;
import com.tms.features.list.marriageinvited.MarriageInvitedView;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

class EngagementInvitedModel {
    private EngagementInvitedView engagementInvitedView;

    private Connection conn;
    public EngagementInvitedModel(EngagementInvitedView engagementInvitedView) throws SQLException, ClassNotFoundException {
        this.engagementInvitedView = engagementInvitedView;
        conn = TmsDB.getInstance().getConnection();
    }

    public ResultSet DisplayAllPeople() throws SQLException {
        try {
            PreparedStatement pre = conn.prepareStatement("SELECT * FROM Invitations WHERE INVITED_STATUS = ?");
            pre.setString(1 , "ENGAGEMENT_INVITED");
            ResultSet set = pre.executeQuery();
            return set;
        }
        catch (Exception e){
            System.out.println(e);
        }
        return null;
    }
}
