package com.tms.data.dto;

/** A label + amount breakdown row under a budget expense. id is null until persisted. */
public class BudgetSplit {

    private Integer id;
    private String label;
    private int amount;

    public BudgetSplit() {}

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getLabel() { return label; }
    public void setLabel(String label) { this.label = label; }

    public int getAmount() { return amount; }
    public void setAmount(int amount) { this.amount = amount; }
}
