package com.tms.servlets;

import com.tms.data.dto.Function;
import com.tms.servlets.actions.FunctionActions;
import com.tms.servlets.message.Message;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.util.ArrayList;

public class FunctionServlet extends JsonServlet {

    @Override
    public Class getModalClass() {
        return Function.class;
    }

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) {
        try {
            FunctionActions actions = new FunctionActions();
            ArrayList<Function> list = actions.getAllFunctions();
            request.setAttribute("OUTPUT", list);
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) {
        try {
            Function function = (Function) request.getAttribute("INPUT");
            FunctionActions actions = new FunctionActions();
            Message message = actions.addFunction(function);
            request.setAttribute("OUTPUT", message);
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    // PUT /api/functions  body: { id, name, color, displayOrder }
    @Override
    public void doPut(HttpServletRequest request, HttpServletResponse response) {
        try {
            Function function = (Function) request.getAttribute("INPUT");
            FunctionActions actions = new FunctionActions();
            Message message = actions.updateFunction(function);
            request.setAttribute("OUTPUT", message);
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    // DELETE /api/functions?id=<id>
    @Override
    public void doDelete(HttpServletRequest request, HttpServletResponse response) {
        try {
            String id = request.getParameter("id");
            FunctionActions actions = new FunctionActions();
            Message message = actions.deleteFunction(Integer.parseInt(id));
            request.setAttribute("OUTPUT", message);
        } catch (Exception e) {
            System.out.println(e);
        }
    }
}
