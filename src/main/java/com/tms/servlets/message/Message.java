package com.tms.servlets.message;

public class Message {

    public enum Status{
        SUCCESS , FAIL
    }
    private Status status;
    private String message;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setStatus(Status status){
        this.status = status;
    }

    public Status getStatus(){
        return status;
    }
}
