package com.tms.data.dto;

public class PersonFunctionStatusDTO {

    private int personId;
    private int functionId;
    private String status;

    public PersonFunctionStatusDTO() {}

    public int getPersonId() { return personId; }
    public void setPersonId(int personId) { this.personId = personId; }

    public int getFunctionId() { return functionId; }
    public void setFunctionId(int functionId) { this.functionId = functionId; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
