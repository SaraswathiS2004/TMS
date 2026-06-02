package com.tms.servlets.actions;

import com.ormx.OrmX;
import com.ormx.db.query.Condition;
import com.tms.data.dto.IncomeItem;
import com.tms.db.Income_Items;
import com.tms.servlets.message.Message;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class IncomeActions {

    public ArrayList<IncomeItem> listAll() {
        List<Map<String, Object>> rows = new ArrayList<>(OrmX.select(Income_Items.TABLE_NAME).fetchRaw());
        rows.sort((a, b) -> {
            int dc = Integer.compare(num(a, Income_Items.DISPLAY_ORDER), num(b, Income_Items.DISPLAY_ORDER));
            if (dc != 0) { return dc; }
            return Integer.compare(num(a, Income_Items.ID), num(b, Income_Items.ID));
        });
        ArrayList<IncomeItem> list = new ArrayList<>();
        for (Map<String, Object> row : rows) {
            IncomeItem item = new IncomeItem();
            item.setId(num(row, Income_Items.ID));
            item.setSource((String) row.get(Income_Items.SOURCE));
            item.setIncomeDate((String) row.get(Income_Items.INCOME_DATE));
            item.setEstimatedAmount(num(row, Income_Items.ESTIMATED_AMOUNT));
            Object actual = row.get(Income_Items.ACTUAL_AMOUNT);
            item.setActualAmount(actual != null ? ((Number) actual).intValue() : null);
            item.setNotes((String) row.get(Income_Items.NOTES));
            item.setDisplayOrder(num(row, Income_Items.DISPLAY_ORDER));
            list.add(item);
        }
        return list;
    }

    public Message addItem(IncomeItem item) {
        Message message = new Message();
        try {
            OrmX.insert(Income_Items.TABLE_NAME)
                .set(Income_Items.SOURCE, item.getSource())
                .set(Income_Items.INCOME_DATE, item.getIncomeDate())
                .set(Income_Items.ESTIMATED_AMOUNT, item.getEstimatedAmount())
                .set(Income_Items.ACTUAL_AMOUNT, item.getActualAmount())
                .set(Income_Items.NOTES, item.getNotes())
                .set(Income_Items.DISPLAY_ORDER, item.getDisplayOrder())
                .execute();
            message.setMessage("Income added.");
            message.setStatus(Message.Status.SUCCESS);
        } catch (Exception e) {
            System.out.println(e);
            message.setStatus(Message.Status.FAIL);
        }
        return message;
    }

    public Message updateItem(IncomeItem item) {
        Message message = new Message();
        try {
            int rows = OrmX.update(Income_Items.TABLE_NAME)
                .set(Income_Items.SOURCE, item.getSource())
                .set(Income_Items.INCOME_DATE, item.getIncomeDate())
                .set(Income_Items.ESTIMATED_AMOUNT, item.getEstimatedAmount())
                .set(Income_Items.ACTUAL_AMOUNT, item.getActualAmount())
                .set(Income_Items.NOTES, item.getNotes())
                .set(Income_Items.DISPLAY_ORDER, item.getDisplayOrder())
                .where(Condition.eq(Income_Items.ID, item.getId()))
                .execute();
            message.setMessage(rows > 0 ? "Income updated." : "Income not found.");
            message.setStatus(rows > 0 ? Message.Status.SUCCESS : Message.Status.FAIL);
        } catch (Exception e) {
            System.out.println(e);
            message.setStatus(Message.Status.FAIL);
        }
        return message;
    }

    public Message deleteItem(int id) {
        Message message = new Message();
        try {
            int rows = OrmX.delete(Income_Items.TABLE_NAME)
                .where(Condition.eq(Income_Items.ID, id))
                .execute();
            message.setMessage(rows > 0 ? "Income deleted." : "Income not found.");
            message.setStatus(rows > 0 ? Message.Status.SUCCESS : Message.Status.FAIL);
        } catch (Exception e) {
            System.out.println(e);
            message.setStatus(Message.Status.FAIL);
        }
        return message;
    }

    private static int num(Map<String, Object> row, String col) {
        return ((Number) row.get(col)).intValue();
    }
}
