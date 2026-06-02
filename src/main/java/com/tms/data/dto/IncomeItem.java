package com.tms.data.dto;

/** A date-driven income source. actualAmount is nullable (not yet received). */
public class IncomeItem {

    private int id;
    private String source;
    private String incomeDate;   // ISO yyyy-MM-dd, nullable
    private int estimatedAmount;
    private Integer actualAmount; // nullable
    private String notes = "";
    private int displayOrder;

    public IncomeItem() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }

    public String getIncomeDate() { return incomeDate; }
    public void setIncomeDate(String incomeDate) { this.incomeDate = incomeDate; }

    public int getEstimatedAmount() { return estimatedAmount; }
    public void setEstimatedAmount(int estimatedAmount) { this.estimatedAmount = estimatedAmount; }

    public Integer getActualAmount() { return actualAmount; }
    public void setActualAmount(Integer actualAmount) { this.actualAmount = actualAmount; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes != null ? notes : ""; }

    public int getDisplayOrder() { return displayOrder; }
    public void setDisplayOrder(int displayOrder) { this.displayOrder = displayOrder; }
}
