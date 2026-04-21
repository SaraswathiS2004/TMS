package com.tms.features.list.bothinvited;



import java.sql.ResultSet;
import java.sql.SQLException;

public class BothInvitedView {
    private BothInvitedModel bothInvitedModel;
    public BothInvitedView() throws SQLException, ClassNotFoundException {
        this.bothInvitedModel = new BothInvitedModel(this);
    }
    public void init() throws SQLException {
        ResultSet set = bothInvitedModel.DisplayAllPeople();
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
