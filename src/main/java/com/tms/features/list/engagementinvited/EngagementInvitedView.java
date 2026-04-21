package com.tms.features.list.engagementinvited;


import java.sql.ResultSet;
import java.sql.SQLException;

public class EngagementInvitedView {
    private EngagementInvitedModel engagementInvitedModel;
    public EngagementInvitedView() throws SQLException, ClassNotFoundException {
        this.engagementInvitedModel = new EngagementInvitedModel(this);
    }
    public void init() throws SQLException {
        ResultSet set = engagementInvitedModel.DisplayAllPeople();
        if(set!= null) {
            int i = 0;
            while (set.next()) {
                if (i == 0) {
                    System.out.println("ID   Name      City    Relation Type     Number Of People  will come   Invited Status");
                    System.out.println("------------------------------------------------------------------------------------------");
                    i++;
                }
                System.out.println(set.getInt("ID") + "   " + set.getString("NAME") + "     " + set.getString("CITY") + "     " + set.getString("NUMBER_OF_PEOPLE_WILL_COME") + "     " + set.getString("INVITED_STATUS"));
            }
        }
    }
}
