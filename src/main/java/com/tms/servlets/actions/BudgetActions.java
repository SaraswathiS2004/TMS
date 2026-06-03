package com.tms.servlets.actions;

import com.ormx.OrmX;
import com.ormx.db.query.Condition;
import com.tms.data.dto.BudgetItem;
import com.tms.data.dto.BudgetSplit;
import com.tms.db.Budget_Item_Splits;
import com.tms.db.Budget_Items;
import com.tms.servlets.message.Message;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class BudgetActions {

    public ArrayList<BudgetItem> listAll() {
        List<Map<String, Object>> rows = new ArrayList<>(OrmX.select(Budget_Items.TABLE_NAME).fetchRaw());
        rows.sort((a, b) -> {
            int fc = Integer.compare(num(a, Budget_Items.FUNCTION_ID), num(b, Budget_Items.FUNCTION_ID));
            if (fc != 0) { return fc; }
            int dc = Integer.compare(num(a, Budget_Items.DISPLAY_ORDER), num(b, Budget_Items.DISPLAY_ORDER));
            if (dc != 0) { return dc; }
            return Integer.compare(num(a, Budget_Items.ID), num(b, Budget_Items.ID));
        });
        Map<Integer, BudgetItem> byId = new LinkedHashMap<>();
        ArrayList<BudgetItem> list = new ArrayList<>();
        for (Map<String, Object> row : rows) {
            BudgetItem item = new BudgetItem();
            int id = num(row, Budget_Items.ID);
            item.setId(id);
            item.setFunctionId(num(row, Budget_Items.FUNCTION_ID));
            item.setName((String) row.get(Budget_Items.NAME));
            item.setEstimatedAmount(num(row, Budget_Items.ESTIMATED_AMOUNT));
            Object actual = row.get(Budget_Items.ACTUAL_AMOUNT);
            item.setActualAmount(actual != null ? ((Number) actual).intValue() : null);
            item.setPaidAmount(num(row, Budget_Items.PAID_AMOUNT));
            item.setNotes((String) row.get(Budget_Items.NOTES));
            item.setDisplayOrder(num(row, Budget_Items.DISPLAY_ORDER));
            byId.put(id, item);
            list.add(item);
        }
        attachSplits(byId);
        return list;
    }

    private void attachSplits(Map<Integer, BudgetItem> byId) {
        if (byId.isEmpty()) { return; }
        List<Map<String, Object>> rows = OrmX.select(Budget_Item_Splits.TABLE_NAME)
            .where(Condition.in(Budget_Item_Splits.BUDGET_ITEM_ID, new ArrayList<>(byId.keySet())))
            .fetchRaw();
        rows.sort((a, b) -> Integer.compare(num(a, Budget_Item_Splits.DISPLAY_ORDER), num(b, Budget_Item_Splits.DISPLAY_ORDER)));
        for (Map<String, Object> row : rows) {
            BudgetItem item = byId.get(num(row, Budget_Item_Splits.BUDGET_ITEM_ID));
            if (item == null) { continue; }
            BudgetSplit split = new BudgetSplit();
            split.setId(num(row, Budget_Item_Splits.ID));
            split.setLabel((String) row.get(Budget_Item_Splits.LABEL));
            split.setAmount(num(row, Budget_Item_Splits.AMOUNT));
            item.getSplits().add(split);
        }
    }

    public Message addItem(BudgetItem item) {
        Message message = new Message();
        if (item == null) {
            message.setMessage("No expense data provided.");
            message.setStatus(Message.Status.FAIL);
            return message;
        }
        try {
            int estimated = effectiveEstimated(item);
            long newId = OrmX.insert(Budget_Items.TABLE_NAME)
                .set(Budget_Items.FUNCTION_ID, item.getFunctionId())
                .set(Budget_Items.NAME, item.getName())
                .set(Budget_Items.ESTIMATED_AMOUNT, estimated)
                .set(Budget_Items.ACTUAL_AMOUNT, item.getActualAmount())
                .set(Budget_Items.PAID_AMOUNT, item.getPaidAmount())
                .set(Budget_Items.NOTES, item.getNotes())
                .set(Budget_Items.DISPLAY_ORDER, item.getDisplayOrder())
                .execute();
            syncSplits((int) newId, item.getSplits());
            message.setMessage("Expense added.");
            message.setStatus(Message.Status.SUCCESS);
        } catch (Exception e) {
            System.out.println(e);
            message.setStatus(Message.Status.FAIL);
        }
        return message;
    }

    public Message updateItem(BudgetItem item) {
        Message message = new Message();
        if (item == null) {
            message.setMessage("No expense data provided.");
            message.setStatus(Message.Status.FAIL);
            return message;
        }
        try {
            int estimated = effectiveEstimated(item);
            int rows = OrmX.update(Budget_Items.TABLE_NAME)
                .set(Budget_Items.NAME, item.getName())
                .set(Budget_Items.ESTIMATED_AMOUNT, estimated)
                .set(Budget_Items.ACTUAL_AMOUNT, item.getActualAmount())
                .set(Budget_Items.PAID_AMOUNT, item.getPaidAmount())
                .set(Budget_Items.NOTES, item.getNotes())
                .set(Budget_Items.DISPLAY_ORDER, item.getDisplayOrder())
                .where(Condition.eq(Budget_Items.ID, item.getId()))
                .execute();
            syncSplits(item.getId(), item.getSplits());
            message.setMessage(rows > 0 ? "Expense updated." : "Expense not found.");
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
            int rows = OrmX.delete(Budget_Items.TABLE_NAME)
                .where(Condition.eq(Budget_Items.ID, id))
                .execute();
            message.setMessage(rows > 0 ? "Expense deleted." : "Expense not found.");
            message.setStatus(rows > 0 ? Message.Status.SUCCESS : Message.Status.FAIL);
        } catch (Exception e) {
            System.out.println(e);
            message.setStatus(Message.Status.FAIL);
        }
        return message;
    }

    /** When split rows are present, the expense estimate is their sum; otherwise the entered estimate. */
    private int effectiveEstimated(BudgetItem item) {
        List<BudgetSplit> splits = validSplits(item.getSplits());
        if (splits.isEmpty()) { return item.getEstimatedAmount(); }
        return splits.stream().mapToInt(BudgetSplit::getAmount).sum();
    }

    private List<BudgetSplit> validSplits(List<BudgetSplit> splits) {
        if (splits == null) { return List.of(); }
        return splits.stream()
            .filter(s -> s.getLabel() != null && !s.getLabel().trim().isEmpty())
            .collect(Collectors.toList());
    }

    /** Reconciles split rows by id: update existing, insert new, delete removed. */
    private void syncSplits(int budgetItemId, List<BudgetSplit> splits) {
        Set<Integer> existingIds = OrmX.select(Budget_Item_Splits.TABLE_NAME)
            .where(Condition.eq(Budget_Item_Splits.BUDGET_ITEM_ID, budgetItemId))
            .fetchRaw().stream()
            .map(r -> num(r, Budget_Item_Splits.ID))
            .collect(Collectors.toSet());

        Set<Integer> keptIds = new HashSet<>();
        int order = 0;
        for (BudgetSplit split : validSplits(splits)) {
            String label = split.getLabel().trim();
            if (split.getId() != null && existingIds.contains(split.getId())) {
                keptIds.add(split.getId());
                OrmX.update(Budget_Item_Splits.TABLE_NAME)
                    .set(Budget_Item_Splits.LABEL, label)
                    .set(Budget_Item_Splits.AMOUNT, split.getAmount())
                    .set(Budget_Item_Splits.DISPLAY_ORDER, order++)
                    .where(Condition.eq(Budget_Item_Splits.ID, split.getId()))
                    .execute();
            } else {
                OrmX.insert(Budget_Item_Splits.TABLE_NAME)
                    .set(Budget_Item_Splits.BUDGET_ITEM_ID, budgetItemId)
                    .set(Budget_Item_Splits.LABEL, label)
                    .set(Budget_Item_Splits.AMOUNT, split.getAmount())
                    .set(Budget_Item_Splits.DISPLAY_ORDER, order++)
                    .execute();
            }
        }

        List<Integer> toRemove = existingIds.stream()
            .filter(id -> !keptIds.contains(id))
            .collect(Collectors.toList());
        if (!toRemove.isEmpty()) {
            OrmX.delete(Budget_Item_Splits.TABLE_NAME)
                .where(Condition.in(Budget_Item_Splits.ID, toRemove))
                .execute();
        }
    }

    private static int num(Map<String, Object> row, String col) {
        return ((Number) row.get(col)).intValue();
    }
}
