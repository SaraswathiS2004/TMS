package com.tms.servlets.actions;

import com.ormx.OrmX;
import com.ormx.db.query.Condition;
import com.tms.data.dto.Function;
import com.tms.db.Functions;
import com.tms.servlets.message.Message;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class FunctionActions {

    public ArrayList<Function> getAllFunctions() {
        List<Map<String, Object>> rows = new ArrayList<>(OrmX.select(Functions.TABLE_NAME).fetchRaw());
        rows.sort((a, b) -> {
            int cmp = Integer.compare(
                ((Number) a.get(Functions.DISPLAY_ORDER)).intValue(),
                ((Number) b.get(Functions.DISPLAY_ORDER)).intValue()
            );
            if (cmp != 0) {
                return cmp;
            }
            return Integer.compare(
                ((Number) a.get(Functions.ID)).intValue(),
                ((Number) b.get(Functions.ID)).intValue()
            );
        });
        ArrayList<Function> list = new ArrayList<>();
        for (Map<String, Object> row : rows) {
            Function f = new Function();
            f.setId(((Number) row.get(Functions.ID)).intValue());
            f.setName((String) row.get(Functions.NAME));
            f.setColor((String) row.get(Functions.COLOR));
            f.setDisplayOrder(((Number) row.get(Functions.DISPLAY_ORDER)).intValue());
            f.setEventDate((String) row.get(Functions.EVENT_DATE));
            list.add(f);
        }
        return list;
    }

    public Message addFunction(Function function) {
        Message message = new Message();
        try {
            OrmX.insert(Functions.TABLE_NAME)
                .set(Functions.NAME, function.getName())
                .set(Functions.COLOR, function.getColor() != null ? function.getColor() : "#4f46e5")
                .set(Functions.DISPLAY_ORDER, function.getDisplayOrder())
                .set(Functions.EVENT_DATE, function.getEventDate())
                .execute();
            message.setMessage("Function added successfully.");
            message.setStatus(Message.Status.SUCCESS);
        } catch (Exception e) {
            System.out.println(e);
            message.setStatus(Message.Status.FAIL);
        }
        return message;
    }

    public Message updateFunction(Function function) {
        Message message = new Message();
        try {
            int rows = OrmX.update(Functions.TABLE_NAME)
                .set(Functions.NAME, function.getName())
                .set(Functions.COLOR, function.getColor() != null ? function.getColor() : "#4f46e5")
                .set(Functions.DISPLAY_ORDER, function.getDisplayOrder())
                .set(Functions.EVENT_DATE, function.getEventDate())
                .where(Condition.eq(Functions.ID, function.getId()))
                .execute();
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
            int rows = OrmX.delete(Functions.TABLE_NAME)
                .where(Condition.eq(Functions.ID, id))
                .execute();
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
