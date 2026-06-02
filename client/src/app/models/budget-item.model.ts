export interface BudgetSplit {
  id?: number;
  label: string;
  amount: number;
}

export interface BudgetItem {
  id?: number;
  functionId: number;
  name: string;
  estimatedAmount: number;
  actualAmount?: number | null;
  paidAmount: number;
  notes?: string;
  displayOrder?: number;
  splits?: BudgetSplit[];
  remaining?: number; // server-computed convenience
}

/** Remaining to pay = (actual, or estimated when blank) − paid. */
export function budgetRemaining(item: BudgetItem): number {
  const base = item.actualAmount != null ? item.actualAmount : (item.estimatedAmount ?? 0);
  return base - (item.paidAmount ?? 0);
}

/** Effective estimate = sum of splits when present, else the entered estimate. */
export function budgetEstimated(item: BudgetItem): number {
  const splits = (item.splits ?? []).filter(s => (s.label ?? '').trim().length > 0);
  if (splits.length > 0) {
    return splits.reduce((sum, s) => sum + (s.amount ?? 0), 0);
  }
  return item.estimatedAmount ?? 0;
}
