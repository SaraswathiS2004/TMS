package com.tms.features.list.marriageinvited;

import java.util.List;
import java.util.Map;

public class MarriageInvitedView {

    private final MarriageInvitedModel marriageInvitedModel = new MarriageInvitedModel();

    public void init() {
        List<Map<String, Object>> rows = marriageInvitedModel.displayAllPeople();
        if (!rows.isEmpty()) {
            System.out.println("ID   Name      City    Relation Type     Number Of People  will come   Invited Status");
            System.out.println("------------------------------------------------------------------------------------------");
            for (Map<String, Object> row : rows) {
                System.out.println(row.get("ID") + "   " + row.get("NAME") + "     " + row.get("CITY") +
                    "     " + row.get("NUMBER_OF_PEOPLE_WILL_COME") + "     " + row.get("INVITED_STATUS"));
            }
        }
    }
}
