package com.tms.servlets;

import com.tms.data.dto.BudgetItem;
import com.tms.servlets.actions.BudgetActions;
import com.tms.servlets.message.Message;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.util.ArrayList;

public class BudgetServlet extends JsonServlet {

    @Override
    public Class getModalClass() {
        return BudgetItem.class;
    }

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) {
        try {
            BudgetActions actions = new BudgetActions();
            ArrayList<BudgetItem> list = actions.listAll();
            request.setAttribute("OUTPUT", list);
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) {
        try {
            BudgetItem item = (BudgetItem) request.getAttribute("INPUT");
            Message message = new BudgetActions().addItem(item);
            request.setAttribute("OUTPUT", message);
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    @Override
    public void doPut(HttpServletRequest request, HttpServletResponse response) {
        try {
            BudgetItem item = (BudgetItem) request.getAttribute("INPUT");
            Message message = new BudgetActions().updateItem(item);
            request.setAttribute("OUTPUT", message);
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    // DELETE /api/budget?id=<id>
    @Override
    public void doDelete(HttpServletRequest request, HttpServletResponse response) {
        try {
            String id = request.getParameter("id");
            Message message = new BudgetActions().deleteItem(Integer.parseInt(id));
            request.setAttribute("OUTPUT", message);
        } catch (Exception e) {
            System.out.println(e);
        }
    }
}
