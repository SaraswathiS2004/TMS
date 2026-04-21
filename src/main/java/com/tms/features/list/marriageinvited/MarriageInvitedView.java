package com.tms.features.list.marriageinvited;


import java.sql.ResultSet;
import java.sql.SQLException;

public class MarriageInvitedView {
    private MarriageInvitedModel marriageInvitedModel;
    public MarriageInvitedView() throws SQLException, ClassNotFoundException {
        this.marriageInvitedModel = new MarriageInvitedModel(this);
    }
    public void init() throws SQLException {
        ResultSet set = marriageInvitedModel.DisplayAllPeople();
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
