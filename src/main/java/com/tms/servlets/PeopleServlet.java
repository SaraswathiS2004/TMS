package com.tms.servlets;

import com.tms.data.dto.People;
import com.tms.servlets.actions.PeopleActions;
import com.tms.servlets.message.Message;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.util.ArrayList;

public class PeopleServlet extends JsonServlet {

    @Override
    public Class getModalClass() {
        return People.class;
    }

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) {
        try {
            People people = (People) request.getAttribute("INPUT");
            PeopleActions peopleActions = new PeopleActions();
            Message message = peopleActions.addPeople(people);
            request.setAttribute("OUTPUT", message);
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) {
        try {
            String id = request.getParameter("id");
            String type = request.getParameter("type");
            PeopleActions peopleActions = new PeopleActions();
            ArrayList<People> listOfPeople = peopleActions.listPeople(id, type);
            request.setAttribute("OUTPUT", listOfPeople);
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    // Handles PUT /tms/people — update a person's invitation status
    @Override
    public void doPut(HttpServletRequest request, HttpServletResponse response) {
        try {
            People people = (People) request.getAttribute("INPUT");
            PeopleActions peopleActions = new PeopleActions();
            Message message = peopleActions.markAsInvited(people.getId(), people.getInvitedStatus());
            request.setAttribute("OUTPUT", message);
        } catch (Exception e) {
            System.out.println(e);
        }
    }
}
