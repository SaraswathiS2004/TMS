package com.tms.data.dto;

/** A gift a guest gave for a specific function. value is nullable (e.g. an item with no set worth). */
public class Gift {

    private int id;
    private int personId;
    private int functionId;
    private GiftType giftType = GiftType.CASH;
    private Integer value;       // nullable
    private String description = "";
    private String giftDate;     // ISO yyyy-MM-dd, nullable
    private String notes = "";
    private int displayOrder;

    public Gift() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getPersonId() { return personId; }
    public void setPersonId(int personId) { this.personId = personId; }

    public int getFunctionId() { return functionId; }
    public void setFunctionId(int functionId) { this.functionId = functionId; }

    public GiftType getGiftType() { return giftType; }
    public void setGiftType(GiftType giftType) { this.giftType = giftType; }

    public Integer getValue() { return value; }
    public void setValue(Integer value) { this.value = value; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description != null ? description : ""; }

    public String getGiftDate() { return giftDate; }
    public void setGiftDate(String giftDate) { this.giftDate = giftDate; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes != null ? notes : ""; }

    public int getDisplayOrder() { return displayOrder; }
    public void setDisplayOrder(int displayOrder) { this.displayOrder = displayOrder; }
}
