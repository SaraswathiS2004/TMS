package com.tms.servlets;

import com.tms.data.dto.Gift;
import com.tms.servlets.actions.GiftActions;
import com.tms.servlets.message.Message;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.util.ArrayList;

public class GiftServlet extends JsonServlet {

    @Override
    public Class getModalClass() {
        return Gift.class;
    }

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) {
        try {
            ArrayList<Gift> list = new GiftActions().listAll();
            request.setAttribute("OUTPUT", list);
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) {
        try {
            Gift gift = (Gift) request.getAttribute("INPUT");
            Message message = new GiftActions().addItem(gift);
            request.setAttribute("OUTPUT", message);
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    @Override
    public void doPut(HttpServletRequest request, HttpServletResponse response) {
        try {
            Gift gift = (Gift) request.getAttribute("INPUT");
            Message message = new GiftActions().updateItem(gift);
            request.setAttribute("OUTPUT", message);
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    // DELETE /api/gifts?id=<id>
    @Override
    public void doDelete(HttpServletRequest request, HttpServletResponse response) {
        try {
            String id = request.getParameter("id");
            Message message = new GiftActions().deleteItem(Integer.parseInt(id));
            request.setAttribute("OUTPUT", message);
        } catch (Exception e) {
            System.out.println(e);
        }
    }
}
