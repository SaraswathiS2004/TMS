package com.tms.servlets.actions;

import com.ormx.OrmX;
import com.ormx.db.query.Condition;
import com.tms.data.dto.Function;
import com.tms.db.FunctionsTable;
import com.tms.servlets.message.Message;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class FunctionActions {

    public ArrayList<Function> getAllFunctions() {
        List<Map<String, Object>> rows = new ArrayList<>(OrmX.select(FunctionsTable.TABLE_NAME).fetchRaw());
        rows.sort((a, b) -> {
            int cmp = Integer.compare(
                ((Number) a.get(FunctionsTable.DISPLAY_ORDER)).intValue(),
                ((Number) b.get(FunctionsTable.DISPLAY_ORDER)).intValue()
            );
            if (cmp != 0) {
                return cmp;
            }
            return Integer.compare(
                ((Number) a.get(FunctionsTable.ID)).intValue(),
                ((Number) b.get(FunctionsTable.ID)).intValue()
            );
        });
        ArrayList<Function> list = new ArrayList<>();
        for (Map<String, Object> row : rows) {
            Function f = new Function();
            f.setId(((Number) row.get(FunctionsTable.ID)).intValue());
            f.setName((String) row.get(FunctionsTable.NAME));
            f.setColor((String) row.get(FunctionsTable.COLOR));
            f.setDisplayOrder(((Number) row.get(FunctionsTable.DISPLAY_ORDER)).intValue());
            list.add(f);
        }
        return list;
    }

    public Message addFunction(Function function) {
        Message message = new Message();
        try {
            OrmX.insert(FunctionsTable.TABLE_NAME)
                .set(FunctionsTable.NAME, function.getName())
                .set(FunctionsTable.COLOR, function.getColor() != null ? function.getColor() : "#4f46e5")
                .set(FunctionsTable.DISPLAY_ORDER, function.getDisplayOrder())
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
            int rows = OrmX.update(FunctionsTable.TABLE_NAME)
                .set(FunctionsTable.NAME, function.getName())
                .set(FunctionsTable.COLOR, function.getColor() != null ? function.getColor() : "#4f46e5")
                .set(FunctionsTable.DISPLAY_ORDER, function.getDisplayOrder())
                .where(Condition.eq(FunctionsTable.ID, function.getId()))
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
            int rows = OrmX.delete(FunctionsTable.TABLE_NAME)
                .where(Condition.eq(FunctionsTable.ID, id))
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
