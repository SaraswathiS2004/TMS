export interface IncomeItem {
  id?: number;
  source: string;
  incomeDate?: string | null;  // ISO yyyy-MM-dd
  estimatedAmount: number;
  actualAmount?: number | null;
  notes?: string;
  displayOrder?: number;
}
