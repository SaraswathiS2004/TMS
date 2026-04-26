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
    public void doGet(HttpServletRequest request, HttpServletResponse response) {
        try {
            String id = request.getParameter("id");
            String functionId = request.getParameter("functionId");
            String noFunction = request.getParameter("noFunction");
            PeopleActions actions = new PeopleActions();
            ArrayList<People> list = actions.listPeople(id, functionId, noFunction);
            request.setAttribute("OUTPUT", list);
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) {
        try {
            People people = (People) request.getAttribute("INPUT");
            PeopleActions actions = new PeopleActions();
            Message message = actions.addPeople(people);
            request.setAttribute("OUTPUT", message);
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    // PUT — update a person's invited functions: body { id, invitedFunctionIds: [...] }
    @Override
    public void doPut(HttpServletRequest request, HttpServletResponse response) {
        try {
            People people = (People) request.getAttribute("INPUT");
            PeopleActions actions = new PeopleActions();
            Message message = actions.updateFunctionInvitations(
                people.getId(), people.getInvitedFunctionIds()
            );
            request.setAttribute("OUTPUT", message);
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    // DELETE /api/people?id=<id>
    @Override
    public void doDelete(HttpServletRequest request, HttpServletResponse response) {
        try {
            String id = request.getParameter("id");
            PeopleActions actions = new PeopleActions();
            Message message = actions.deletePeople(Integer.parseInt(id));
            request.setAttribute("OUTPUT", message);
        } catch (Exception e) {
            System.out.println(e);
        }
    }
}
