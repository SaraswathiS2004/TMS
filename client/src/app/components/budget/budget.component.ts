import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { BudgetService } from '../../services/budget.service';
import { FunctionService } from '../../services/function.service';
import { BudgetItem, budgetRemaining, budgetEstimated } from '../../models/budget-item.model';
import { TmsFunction } from '../../models/function.model';
import { TranslatePipe } from '../../pipes/translate.pipe';
import { TranslateService } from '../../services/translate.service';

interface FunctionBudget {
  fn: TmsFunction;
  items: BudgetItem[];
}

@Component({
  selector: 'app-budget',
  standalone: true,
  imports: [CommonModule, RouterLink, FormsModule, TranslatePipe],
  templateUrl: './budget.component.html',
  styleUrl: './budget.component.css'
})
export class BudgetComponent implements OnInit {

  functions: TmsFunction[] = [];
  items: BudgetItem[] = [];
  groups: FunctionBudget[] = [];
  isLoading = true;

  // working copy for add/edit
  editing: BudgetItem | null = null;
  editingId: number | null = null;       // null while adding
  deleteConfirmId: number | null = null;
  expandedId: number | null = null;       // row whose splits are shown
  activeTabId: number | null = null;      // function tab currently being viewed

  feedbackMessage = '';
  feedbackType: 'success' | 'error' = 'success';

  readonly remaining = budgetRemaining;
  readonly estimated = budgetEstimated;

  constructor(
    private budgetService: BudgetService,
    private functionService: FunctionService,
    private translateService: TranslateService
  ) {}

  ngOnInit(): void { this.loadAll(); }

  loadAll(): void {
    this.isLoading = true;
    this.editing = null;
    this.deleteConfirmId = null;
    this.functionService.getAllFunctions().subscribe({
      next: (fns) => {
        this.functions = fns;
        this.budgetService.getAll().subscribe({
          next: (items) => { this.items = items; this.regroup(); this.isLoading = false; },
          error: () => { this.isLoading = false; }
        });
      },
      error: () => { this.isLoading = false; }
    });
  }

  private regroup(): void {
    this.groups = this.functions.map(fn => ({
      fn,
      items: this.items.filter(i => i.functionId === fn.id)
    }));
    // Keep the current tab if it still exists; otherwise default to the first function.
    const stillExists = this.groups.some(g => g.fn.id === this.activeTabId);
    if (!stillExists) { this.activeTabId = this.groups.length > 0 ? this.groups[0].fn.id : null; }
  }

  // ── Function tabs ──
  get activeGroup(): FunctionBudget | null {
    return this.groups.find(g => g.fn.id === this.activeTabId) ?? null;
  }

  selectTab(fnId: number): void {
    if (this.activeTabId === fnId) { return; }
    this.activeTabId = fnId;
    this.cancelEdit();
    this.deleteConfirmId = null;
  }

  // ── Totals ──
  fnTotal(group: FunctionBudget, field: 'est' | 'paid' | 'remaining'): number {
    return group.items.reduce((sum, i) => sum + this.itemValue(i, field), 0);
  }

  grandTotal(field: 'est' | 'paid' | 'remaining'): number {
    return this.items.reduce((sum, i) => sum + this.itemValue(i, field), 0);
  }

  private itemValue(i: BudgetItem, field: 'est' | 'paid' | 'remaining'): number {
    if (field === 'est') { return this.estimated(i); }
    if (field === 'paid') { return i.paidAmount ?? 0; }
    return this.remaining(i);
  }

  isOverspent(item: BudgetItem): boolean {
    return item.actualAmount != null && item.actualAmount > this.estimated(item);
  }

  // ── Expand splits (read-only view) ──
  toggleExpand(item: BudgetItem): void {
    this.expandedId = this.expandedId === item.id ? null : (item.id ?? null);
  }

  // ── Add / Edit ──
  startAdd(fnId: number): void {
    this.editing = { functionId: fnId, name: '', estimatedAmount: 0, actualAmount: null, paidAmount: 0, notes: '', splits: [] };
    this.editingId = null;
    this.deleteConfirmId = null;
  }

  startEdit(item: BudgetItem): void {
    this.editing = {
      ...item,
      splits: (item.splits ?? []).map(s => ({ ...s }))
    };
    this.editingId = item.id ?? null;
    this.deleteConfirmId = null;
  }

  isEditingRow(item: BudgetItem): boolean {
    return this.editing != null && this.editingId === item.id;
  }

  isAddingTo(fnId: number): boolean {
    return this.editing != null && this.editingId === null && this.editing.functionId === fnId;
  }

  addSplitRow(): void {
    if (!this.editing) { return; }
    (this.editing.splits ??= []).push({ label: '', amount: 0 });
    this.onSplitChange();
  }

  removeSplitRow(index: number): void {
    this.editing?.splits?.splice(index, 1);
    this.onSplitChange();
  }

  /** Audit-driven: actual never sits below the split total. Client-side, in the form only. */
  onSplitChange(): void {
    if (!this.editing) { return; }
    const total = this.splitsTotal(this.editing);
    if (total <= 0) { return; }
    const current = Number(this.editing.actualAmount) || 0;
    this.editing.actualAmount = Math.max(current, total);
  }

  hasSplits(item: BudgetItem | null): boolean {
    return !!item && (item.splits ?? []).some(s => (s.label ?? '').trim().length > 0);
  }

  cancelEdit(): void { this.editing = null; this.editingId = null; }

  save(): void {
    if (!this.editing || !this.editing.name.trim()) { return; }
    const payload: BudgetItem = {
      ...this.editing,
      name: this.editing.name.trim(),
      estimatedAmount: this.editing.estimatedAmount || 0,
      paidAmount: this.editing.paidAmount || 0,
      actualAmount: this.editing.actualAmount === null || this.editing.actualAmount === undefined
        || (this.editing.actualAmount as any) === '' ? null : Number(this.editing.actualAmount),
      splits: (this.editing.splits ?? []).filter(s => (s.label ?? '').trim().length > 0)
    };
    const call = this.editingId != null
      ? this.budgetService.update(payload)
      : this.budgetService.add(payload);
    call.subscribe({
      next: (resp) => {
        this.showFeedback(resp.status === 'SUCCESS' ? 'budget.saveSuccess' : 'budget.saveError',
          resp.status === 'SUCCESS' ? 'success' : 'error');
        if (resp.status === 'SUCCESS') { this.loadAll(); }
      },
      error: () => { this.showFeedback('budget.saveError', 'error'); }
    });
  }

  requestDelete(id: number): void { this.deleteConfirmId = id; this.editing = null; }
  cancelDelete(): void { this.deleteConfirmId = null; }

  confirmDelete(id: number): void {
    this.budgetService.delete(id).subscribe({
      next: (resp) => {
        this.showFeedback(resp.status === 'SUCCESS' ? 'budget.deleteSuccess' : 'budget.deleteError',
          resp.status === 'SUCCESS' ? 'success' : 'error');
        this.loadAll();
      },
      error: () => { this.showFeedback('budget.deleteError', 'error'); }
    });
  }

  splitsTotal(item: BudgetItem | null): number {
    return (item?.splits ?? []).reduce((sum, s) => sum + (s.amount ?? 0), 0);
  }

  private showFeedback(key: string, type: 'success' | 'error'): void {
    this.feedbackMessage = this.translateService.translate(key);
    this.feedbackType = type;
    setTimeout(() => { this.feedbackMessage = ''; }, 3000);
  }
}
