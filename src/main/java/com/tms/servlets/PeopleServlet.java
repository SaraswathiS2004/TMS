package com.tms.servlets;

import com.tms.data.dto.People;
import com.tms.features.peopleAdded.PeopleAddedModel;
import com.tms.servlets.actions.PeopleActions;
import com.tms.servlets.message.Message;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;


public class PeopleServlet extends JsonServlet {

    public Class getModalClass(){

        return People.class;
    }

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) {

        try{
            People people = (People) request.getAttribute("INPUT");
            PeopleActions peopleActions = new PeopleActions();
            Message message = peopleActions.addPeople(people);
            request.setAttribute("OUTPUT" , message);

        }
        catch (Exception e){
            System.out.println(e);
        }

    }
    @Override
    public void doGet(HttpServletRequest request , HttpServletResponse response){

    }
}
