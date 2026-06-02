package com.tms.servlets;

import com.tms.data.dto.PersonFunctionStatusDTO;
import com.tms.servlets.actions.PeopleActions;
import com.tms.servlets.message.Message;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class InvitationPersonFunctionServlet extends JsonServlet {

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) {

    }

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) {

    }

    @Override
    public Class getModalClass() {
        return PersonFunctionStatusDTO.class;
    }

    // PUT /api/invitation-person-function  body: { personId, functionId, status }
    // personId here is the Invitation_Persons row id (an individual named person).
    @Override
    public void doPut(HttpServletRequest request, HttpServletResponse response) {
        try {
            PersonFunctionStatusDTO dto = (PersonFunctionStatusDTO) request.getAttribute("INPUT");
            PeopleActions actions = new PeopleActions();
            Message message = actions.updatePersonInvitedStatus(dto.getPersonId(), dto.getFunctionId(), dto.getStatus());
            request.setAttribute("OUTPUT", message);
        } catch (Exception e) {
            System.out.println(e);
        }
    }
}
