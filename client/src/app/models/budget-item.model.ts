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

/** Estimate is human-entered only; splits are audit detail and do not drive it. */
export function budgetEstimated(item: BudgetItem): number {
  return item.estimatedAmount ?? 0;
}

/**
 * Effective actual for calculations only: when actual is blank, fall back to the estimate.
 * This is never persisted — the DB keeps ACTUAL_AMOUNT null until the user enters one.
 */
export function budgetEffectiveActual(item: BudgetItem): number {
  return item.actualAmount != null ? item.actualAmount : (item.estimatedAmount ?? 0);
}
