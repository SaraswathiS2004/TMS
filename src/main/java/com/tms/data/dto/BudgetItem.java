package com.tms.data.dto;

import java.util.ArrayList;
import java.util.List;

/**
 * A budget expense line item under a function. {@code actualAmount} is nullable — when blank the
 * remaining amount falls back to estimated − paid.
 */
public class BudgetItem {

    private int id;
    private int functionId;
    private String name;
    private int estimatedAmount;
    private Integer actualAmount;   // nullable
    private int paidAmount;
    private String notes = "";
    private int displayOrder;
    private List<BudgetSplit> splits = new ArrayList<>();

    public BudgetItem() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getFunctionId() { return functionId; }
    public void setFunctionId(int functionId) { this.functionId = functionId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public int getEstimatedAmount() { return estimatedAmount; }
    public void setEstimatedAmount(int estimatedAmount) { this.estimatedAmount = estimatedAmount; }

    public Integer getActualAmount() { return actualAmount; }
    public void setActualAmount(Integer actualAmount) { this.actualAmount = actualAmount; }

    public int getPaidAmount() { return paidAmount; }
    public void setPaidAmount(int paidAmount) { this.paidAmount = paidAmount; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes != null ? notes : ""; }

    public int getDisplayOrder() { return displayOrder; }
    public void setDisplayOrder(int displayOrder) { this.displayOrder = displayOrder; }

    public List<BudgetSplit> getSplits() { return splits; }
    public void setSplits(List<BudgetSplit> splits) { this.splits = splits; }

    /** Remaining to pay = (actual, or estimated when actual is blank) − paid. */
    public int getRemaining() {
        int base = actualAmount != null ? actualAmount : estimatedAmount;
        return base - paidAmount;
    }
}
