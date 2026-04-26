package com.tms.servlets.actions;

import com.tms.data.dto.Function;
import com.tms.data.respository.TmsDB;
import com.tms.servlets.message.Message;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class FunctionActions {

    private Connection conn;

    public FunctionActions() throws SQLException, ClassNotFoundException {
        conn = TmsDB.getInstance().getConnection();
    }

    public ArrayList<Function> getAllFunctions() throws SQLException {
        PreparedStatement pre = conn.prepareStatement(
            "SELECT ID, NAME, COLOR, DISPLAY_ORDER FROM Functions ORDER BY DISPLAY_ORDER, ID"
        );
        ResultSet rs = pre.executeQuery();
        ArrayList<Function> list = new ArrayList<>();
        while (rs.next()) {
            Function f = new Function();
            f.setId(rs.getInt("ID"));
            f.setName(rs.getString("NAME"));
            f.setColor(rs.getString("COLOR"));
            f.setDisplayOrder(rs.getInt("DISPLAY_ORDER"));
            list.add(f);
        }
        return list;
    }

    public Message addFunction(Function function) {
        Message message = new Message();
        try {
            PreparedStatement pre = conn.prepareStatement(
                "INSERT INTO Functions (NAME, COLOR, DISPLAY_ORDER) VALUES (?, ?, ?)"
            );
            pre.setString(1, function.getName());
            pre.setString(2, function.getColor() != null ? function.getColor() : "#4f46e5");
            pre.setInt(3, function.getDisplayOrder());
            int rows = pre.executeUpdate();
            if (rows > 0) {
                message.setMessage("Function added successfully.");
                message.setStatus(Message.Status.SUCCESS);
            } else {
                message.setMessage("Could not add function.");
                message.setStatus(Message.Status.FAIL);
            }
        } catch (Exception e) {
            System.out.println(e);
            message.setStatus(Message.Status.FAIL);
        }
        return message;
    }

    public Message updateFunction(Function function) {
        Message message = new Message();
        try {
            PreparedStatement pre = conn.prepareStatement(
                "UPDATE Functions SET NAME=?, COLOR=?, DISPLAY_ORDER=? WHERE ID=?"
            );
            pre.setString(1, function.getName());
            pre.setString(2, function.getColor() != null ? function.getColor() : "#4f46e5");
            pre.setInt(3, function.getDisplayOrder());
            pre.setInt(4, function.getId());
            int rows = pre.executeUpdate();
            if (rows > 0) {
                message.setMessage("Function updated.");
                message.setStatus(Message.Status.SUCCESS);
            } else {
                message.setMessage("Function not found.");
                message.setStatus(Message.Status.FAIL);
            }
        } catch (Exception e) {
            System.out.println(e);
            message.setStatus(Message.Status.FAIL);
        }
        return message;
    }

    public Message deleteFunction(int id) {
        Message message = new Message();
        try {
            PreparedStatement pre = conn.prepareStatement("DELETE FROM Functions WHERE ID = ?");
            pre.setInt(1, id);
            int rows = pre.executeUpdate();
            if (rows > 0) {
                message.setMessage("Function deleted.");
                message.setStatus(Message.Status.SUCCESS);
            } else {
                message.setMessage("Function not found.");
                message.setStatus(Message.Status.FAIL);
            }
        } catch (Exception e) {
            System.out.println(e);
            message.setStatus(Message.Status.FAIL);
        }
        return message;
    }
}
