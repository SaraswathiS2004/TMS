package com.tms.servlets.actions;

import com.ormx.OrmX;
import com.ormx.db.query.Condition;
import com.tms.data.dto.GuestGroup;
import com.tms.db.Guest_Groups;
import com.tms.db.Invitations;
import com.tms.servlets.message.Message;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class GuestGroupActions {

    private static final String DEFAULT_COLOR = "#4f46e5";

    public ArrayList<GuestGroup> getAllGroups() {
        List<Map<String, Object>> rows = new ArrayList<>(OrmX.select(Guest_Groups.TABLE_NAME).fetchRaw());
        rows.sort((a, b) -> {
            int cmp = Integer.compare(
                ((Number) a.get(Guest_Groups.DISPLAY_ORDER)).intValue(),
                ((Number) b.get(Guest_Groups.DISPLAY_ORDER)).intValue()
            );
            if (cmp != 0) {
                return cmp;
            }
            return Integer.compare(
                ((Number) a.get(Guest_Groups.ID)).intValue(),
                ((Number) b.get(Guest_Groups.ID)).intValue()
            );
        });
        ArrayList<GuestGroup> list = new ArrayList<>();
        for (Map<String, Object> row : rows) {
            GuestGroup g = new GuestGroup();
            g.setId(((Number) row.get(Guest_Groups.ID)).intValue());
            g.setName((String) row.get(Guest_Groups.NAME));
            g.setColor((String) row.get(Guest_Groups.COLOR));
            g.setDisplayOrder(((Number) row.get(Guest_Groups.DISPLAY_ORDER)).intValue());
            list.add(g);
        }
        return list;
    }

    public Message addGroup(GuestGroup group) {
        Message message = new Message();
        try {
            OrmX.insert(Guest_Groups.TABLE_NAME)
                .set(Guest_Groups.NAME, group.getName())
                .set(Guest_Groups.COLOR, group.getColor() != null ? group.getColor() : DEFAULT_COLOR)
                .set(Guest_Groups.DISPLAY_ORDER, group.getDisplayOrder())
                .execute();
            message.setMessage("Group added successfully.");
            message.setStatus(Message.Status.SUCCESS);
        } catch (Exception e) {
            System.out.println(e);
            message.setStatus(Message.Status.FAIL);
        }
        return message;
    }

    public Message updateGroup(GuestGroup group) {
        Message message = new Message();
        try {
            int rows = OrmX.update(Guest_Groups.TABLE_NAME)
                .set(Guest_Groups.NAME, group.getName())
                .set(Guest_Groups.COLOR, group.getColor() != null ? group.getColor() : DEFAULT_COLOR)
                .set(Guest_Groups.DISPLAY_ORDER, group.getDisplayOrder())
                .where(Condition.eq(Guest_Groups.ID, group.getId()))
                .execute();
            if (rows > 0) {
                message.setMessage("Group updated.");
                message.setStatus(Message.Status.SUCCESS);
            } else {
                message.setMessage("Group not found.");
                message.setStatus(Message.Status.FAIL);
            }
        } catch (Exception e) {
            System.out.println(e);
            message.setStatus(Message.Status.FAIL);
        }
        return message;
    }

    public Message deleteGroup(int id) {
        Message message = new Message();
        try {
            // Detach guests from the group first (GROUP_ID has no DB FK).
            OrmX.update(Invitations.TABLE_NAME)
                .set(Invitations.GROUP_ID, null)
                .where(Condition.eq(Invitations.GROUP_ID, id))
                .execute();
            int rows = OrmX.delete(Guest_Groups.TABLE_NAME)
                .where(Condition.eq(Guest_Groups.ID, id))
                .execute();
            if (rows > 0) {
                message.setMessage("Group deleted.");
                message.setStatus(Message.Status.SUCCESS);
            } else {
                message.setMessage("Group not found.");
                message.setStatus(Message.Status.FAIL);
            }
        } catch (Exception e) {
            System.out.println(e);
            message.setStatus(Message.Status.FAIL);
        }
        return message;
    }
}
