package com.tms.servlets.actions;

import com.ormx.OrmX;
import com.ormx.db.query.Condition;
import com.tms.data.dto.Gift;
import com.tms.data.dto.GiftType;
import com.tms.db.Gift_Items;
import com.tms.servlets.message.Message;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class GiftActions {

    public ArrayList<Gift> listAll() {
        List<Map<String, Object>> rows = new ArrayList<>(OrmX.select(Gift_Items.TABLE_NAME).fetchRaw());
        rows.sort((a, b) -> {
            int pc = Integer.compare(num(a, Gift_Items.PERSON_ID), num(b, Gift_Items.PERSON_ID));
            if (pc != 0) { return pc; }
            int fc = Integer.compare(num(a, Gift_Items.FUNCTION_ID), num(b, Gift_Items.FUNCTION_ID));
            if (fc != 0) { return fc; }
            return Integer.compare(num(a, Gift_Items.ID), num(b, Gift_Items.ID));
        });
        ArrayList<Gift> list = new ArrayList<>();
        for (Map<String, Object> row : rows) {
            Gift gift = new Gift();
            gift.setId(num(row, Gift_Items.ID));
            gift.setPersonId(num(row, Gift_Items.PERSON_ID));
            gift.setFunctionId(num(row, Gift_Items.FUNCTION_ID));
            gift.setGiftType(GiftType.valueOf((String) row.get(Gift_Items.GIFT_TYPE)));
            Object value = row.get(Gift_Items.VALUE);
            gift.setValue(value != null ? ((Number) value).intValue() : null);
            gift.setDescription((String) row.get(Gift_Items.DESCRIPTION));
            gift.setGiftDate((String) row.get(Gift_Items.GIFT_DATE));
            gift.setNotes((String) row.get(Gift_Items.NOTES));
            gift.setDisplayOrder(num(row, Gift_Items.DISPLAY_ORDER));
            list.add(gift);
        }
        return list;
    }

    public Message addItem(Gift gift) {
        Message message = new Message();
        try {
            OrmX.insert(Gift_Items.TABLE_NAME)
                .set(Gift_Items.PERSON_ID, gift.getPersonId())
                .set(Gift_Items.FUNCTION_ID, gift.getFunctionId())
                .set(Gift_Items.GIFT_TYPE, gift.getGiftType().toString())
                .set(Gift_Items.VALUE, gift.getValue())
                .set(Gift_Items.DESCRIPTION, gift.getDescription())
                .set(Gift_Items.GIFT_DATE, gift.getGiftDate())
                .set(Gift_Items.NOTES, gift.getNotes())
                .set(Gift_Items.DISPLAY_ORDER, gift.getDisplayOrder())
                .execute();
            message.setMessage("Gift added.");
            message.setStatus(Message.Status.SUCCESS);
        } catch (Exception e) {
            System.out.println(e);
            message.setStatus(Message.Status.FAIL);
        }
        return message;
    }

    public Message updateItem(Gift gift) {
        Message message = new Message();
        try {
            int rows = OrmX.update(Gift_Items.TABLE_NAME)
                .set(Gift_Items.PERSON_ID, gift.getPersonId())
                .set(Gift_Items.FUNCTION_ID, gift.getFunctionId())
                .set(Gift_Items.GIFT_TYPE, gift.getGiftType().toString())
                .set(Gift_Items.VALUE, gift.getValue())
                .set(Gift_Items.DESCRIPTION, gift.getDescription())
                .set(Gift_Items.GIFT_DATE, gift.getGiftDate())
                .set(Gift_Items.NOTES, gift.getNotes())
                .set(Gift_Items.DISPLAY_ORDER, gift.getDisplayOrder())
                .where(Condition.eq(Gift_Items.ID, gift.getId()))
                .execute();
            message.setMessage(rows > 0 ? "Gift updated." : "Gift not found.");
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
            int rows = OrmX.delete(Gift_Items.TABLE_NAME)
                .where(Condition.eq(Gift_Items.ID, id))
                .execute();
            message.setMessage(rows > 0 ? "Gift deleted." : "Gift not found.");
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
