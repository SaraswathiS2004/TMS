package com.tms.data.dto;

import com.ormx.OrmX;
import com.tms.db.InvitationsTable;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class People {

    private String name;
    private int numberOfPerson;
    private RelationType relationType;
    private InvitedStatus invitedStatus = InvitedStatus.NOT_INVITED;
    private String city;
    private int id;
    private List<Integer> invitedFunctionIds = new ArrayList<>();
    // key: functionId (as string for JSON compat), value: "INVITED" | "NOT_INVITED"
    private Map<String, String> functionStatuses = new LinkedHashMap<>();

    public People() {}

    // storeData kept for CLI backward compatibility
    public String storeData() {
        try {
            OrmX.insert(InvitationsTable.TABLE_NAME)
                .set(InvitationsTable.NAME, name)
                .set(InvitationsTable.CITY, city)
                .set(InvitationsTable.RELATION_TYPE, relationType.toString())
                .set(InvitationsTable.NUMBER_OF_PEOPLE_WILL_COME, numberOfPerson)
                .set(InvitationsTable.INVITED_STATUS, invitedStatus.toString())
                .execute();
            return "Successfully Added!";
        } catch (Exception e) {
            System.out.println(e);
            return "Cannot add person";
        }
    }

    public InvitedStatus getInvitedStatus() { return invitedStatus; }
    public void setInvitedStatus(InvitedStatus invitedStatus) { this.invitedStatus = invitedStatus; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getNumberOfPerson() { return numberOfPerson; }
    public void setNumberOfPerson(int numberOfPerson) { this.numberOfPerson = numberOfPerson; }

    public RelationType getRelationType() { return relationType; }
    public void setRelationType(RelationType relationType) { this.relationType = relationType; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public List<Integer> getInvitedFunctionIds() { return invitedFunctionIds; }
    public void setInvitedFunctionIds(List<Integer> invitedFunctionIds) {
        this.invitedFunctionIds = invitedFunctionIds != null ? invitedFunctionIds : new ArrayList<>();
    }

    public Map<String, String> getFunctionStatuses() { return functionStatuses; }
    public void setFunctionStatuses(Map<String, String> functionStatuses) {
        this.functionStatuses = functionStatuses != null ? functionStatuses : new LinkedHashMap<>();
    }
}
