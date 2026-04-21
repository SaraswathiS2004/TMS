package com.tms.features.list.allpeople;

import java.sql.ResultSet;
import java.sql.SQLException;

public class AllPeopleView {
    private AllPeopleModel allPeopleModel;
    public AllPeopleView() throws SQLException, ClassNotFoundException {
        this.allPeopleModel = new AllPeopleModel(this);
    }
    public void init() throws SQLException {
        ResultSet set = allPeopleModel.DisplayAllPeople();
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
