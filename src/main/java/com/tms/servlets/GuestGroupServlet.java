package com.tms.servlets;

import com.tms.data.dto.GuestGroup;
import com.tms.servlets.actions.GuestGroupActions;
import com.tms.servlets.message.Message;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.util.ArrayList;

public class GuestGroupServlet extends JsonServlet {

    @Override
    public Class getModalClass() {
        return GuestGroup.class;
    }

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) {
        try {
            GuestGroupActions actions = new GuestGroupActions();
            ArrayList<GuestGroup> list = actions.getAllGroups();
            request.setAttribute("OUTPUT", list);
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) {
        try {
            GuestGroup group = (GuestGroup) request.getAttribute("INPUT");
            GuestGroupActions actions = new GuestGroupActions();
            Message message = actions.addGroup(group);
            request.setAttribute("OUTPUT", message);
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    // PUT /api/guest-groups  body: { id, name, color, displayOrder }
    @Override
    public void doPut(HttpServletRequest request, HttpServletResponse response) {
        try {
            GuestGroup group = (GuestGroup) request.getAttribute("INPUT");
            GuestGroupActions actions = new GuestGroupActions();
            Message message = actions.updateGroup(group);
            request.setAttribute("OUTPUT", message);
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    // DELETE /api/guest-groups?id=<id>
    @Override
    public void doDelete(HttpServletRequest request, HttpServletResponse response) {
        try {
            String id = request.getParameter("id");
            GuestGroupActions actions = new GuestGroupActions();
            Message message = actions.deleteGroup(Integer.parseInt(id));
            request.setAttribute("OUTPUT", message);
        } catch (Exception e) {
            System.out.println(e);
        }
    }
}
