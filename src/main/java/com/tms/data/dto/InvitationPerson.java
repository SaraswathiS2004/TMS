package com.tms.data.dto;

/**
 * A named person (guest) listed under an invitation. {@code id} is null for a person
 * that has been added on the client but not yet persisted.
 */
public class InvitationPerson {

    private Integer id;
    private String name;
    private String note = "";

    public InvitationPerson() {}

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note != null ? note : ""; }
}
