package com.tms.servlets.actions;

import com.tms.data.dto.People;
import com.tms.servlets.message.Message;

public class PeopleActions {



    public Message addPeople(People people){
        Message message = new Message();
        try {
            message.setMessage(people.storeData());
            message.setStatus(Message.Status.SUCCESS);
        }
        catch (Exception e){
            System.out.println(e);
            message.setStatus(Message.Status.FAIL);
        }
        return message;
    }
}
