package com.tms.servlets;

import com.tms.data.dto.IncomeItem;
import com.tms.servlets.actions.IncomeActions;
import com.tms.servlets.message.Message;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.util.ArrayList;

public class IncomeServlet extends JsonServlet {

    @Override
    public Class getModalClass() {
        return IncomeItem.class;
    }

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) {
        try {
            ArrayList<IncomeItem> list = new IncomeActions().listAll();
            request.setAttribute("OUTPUT", list);
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) {
        try {
            IncomeItem item = (IncomeItem) request.getAttribute("INPUT");
            Message message = new IncomeActions().addItem(item);
            request.setAttribute("OUTPUT", message);
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    @Override
    public void doPut(HttpServletRequest request, HttpServletResponse response) {
        try {
            IncomeItem item = (IncomeItem) request.getAttribute("INPUT");
            Message message = new IncomeActions().updateItem(item);
            request.setAttribute("OUTPUT", message);
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    // DELETE /api/income?id=<id>
    @Override
    public void doDelete(HttpServletRequest request, HttpServletResponse response) {
        try {
            String id = request.getParameter("id");
            Message message = new IncomeActions().deleteItem(Integer.parseInt(id));
            request.setAttribute("OUTPUT", message);
        } catch (Exception e) {
            System.out.println(e);
        }
    }
}
