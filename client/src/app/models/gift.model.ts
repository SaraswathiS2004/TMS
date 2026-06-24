export type GiftType = 'CASH' | 'ITEM';

export interface Gift {
  id?: number;
  personId: number;
  functionId: number;
  giftType: GiftType;
  value?: number | null;       // optional ₹ value (blank allowed, esp. for items)
  description?: string;
  giftDate?: string | null;    // ISO yyyy-MM-dd
  notes?: string;
  displayOrder?: number;
}
