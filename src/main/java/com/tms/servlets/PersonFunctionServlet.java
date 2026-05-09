package com.tms.servlets;

import com.tms.data.dto.PersonFunctionStatusDTO;
import com.tms.servlets.actions.PeopleActions;
import com.tms.servlets.message.Message;
import com.tms.sheet.impl.PersonFunctionsTable;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class PersonFunctionServlet extends JsonServlet {

    @Override
    public void doPost(HttpServletRequest request , HttpServletResponse response){

    }
    @Override
    public void doGet(HttpServletRequest request , HttpServletResponse response){

    }

    @Override
    public Class getModalClass() {
        return PersonFunctionStatusDTO.class;
    }

    // PUT /api/person-function  body: { personId, functionId, status }
    @Override
    public void doPut(HttpServletRequest request, HttpServletResponse response) {
        try {
            PersonFunctionStatusDTO dto = (PersonFunctionStatusDTO) request.getAttribute("INPUT");
            PeopleActions actions = new PeopleActions();
            Message message = actions.updateFunctionStatus(dto.getPersonId(), dto.getFunctionId(), dto.getStatus());
            if (Message.Status.SUCCESS.equals(message.getStatus())) {
                new PersonFunctionsTable().triggerSync();
            }
            request.setAttribute("OUTPUT", message);
        } catch (Exception e) {
            System.out.println(e);
        }
    }
}
