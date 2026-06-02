package com.tms.data.dto;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * A named person (guest) listed under an invitation. {@code id} is null for a person
 * that has been added on the client but not yet persisted. {@code functionStatuses} holds
 * this person's per-function invited status (functionId as string → "INVITED"/"NOT_INVITED").
 */
public class InvitationPerson {

    private Integer id;
    private String name;
    private String note = "";
    private Map<String, String> functionStatuses = new LinkedHashMap<>();

    public InvitationPerson() {}

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note != null ? note : ""; }

    public Map<String, String> getFunctionStatuses() { return functionStatuses; }
    public void setFunctionStatuses(Map<String, String> functionStatuses) {
        this.functionStatuses = functionStatuses != null ? functionStatuses : new LinkedHashMap<>();
    }
}
