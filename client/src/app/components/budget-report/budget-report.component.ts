import { Component, OnDestroy, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { forkJoin } from 'rxjs';
import { BudgetService } from '../../services/budget.service';
import { FunctionService } from '../../services/function.service';
import { BudgetItem, budgetEstimated, budgetRemaining } from '../../models/budget-item.model';
import { TmsFunction } from '../../models/function.model';
import { TranslatePipe } from '../../pipes/translate.pipe';
import { TranslateService } from '../../services/translate.service';

interface FunctionSection {
  fn: TmsFunction;
  items: BudgetItem[];
}

type AmountField = 'estimated' | 'actual' | 'paid' | 'balance';

const MAX_BLANK_COLUMNS = 5;

@Component({
  selector: 'app-budget-report',
  standalone: true,
  imports: [CommonModule, FormsModule, TranslatePipe],
  templateUrl: './budget-report.component.html',
  styleUrl: './budget-report.component.css'
})
export class BudgetReportComponent implements OnInit, OnDestroy {

  sections: FunctionSection[] = [];
  items: BudgetItem[] = [];
  isLoading = true;
  generatedOn = new Date();

  // 'ALL' or a function id — limits the report to one function
  selectedFunctionId: number | 'ALL' = 'ALL';

  // Amount column selection — all on by default
  showEstimated = true;
  showActual = true;
  showPaid = true;
  showBalance = true;

  // Extra detail under the item name
  includeSplits = false;
  includeNotes = false;

  // Blank pen-fillable columns
  readonly maxBlankColumns = MAX_BLANK_COLUMNS;
  blankCount = 0;
  blankLabels: string[] = Array(MAX_BLANK_COLUMNS).fill('');

  readonly estimated = budgetEstimated;
  readonly remaining = budgetRemaining;

  private previousTitle = '';

  constructor(
    private budgetService: BudgetService,
    private functionService: FunctionService,
    private translateService: TranslateService
  ) {}

  ngOnInit(): void {
    this.previousTitle = document.title;
    document.title = this.translateService.translate('report.budgetTitle')
      + ' - ' + this.generatedOn.toISOString().slice(0, 10);
    forkJoin({
      fns: this.functionService.getAllFunctions(),
      items: this.budgetService.getAll()
    }).subscribe({
      next: ({ fns, items }) => {
        this.items = items;
        this.sections = fns
          .map(fn => ({ fn, items: items.filter(i => i.functionId === fn.id) }))
          .filter(section => section.items.length > 0);
        this.isLoading = false;
      },
      error: () => { this.isLoading = false; }
    });
  }

  ngOnDestroy(): void {
    document.title = this.previousTitle;
  }

  get visibleSections(): FunctionSection[] {
    if (this.selectedFunctionId === 'ALL') { return this.sections; }
    return this.sections.filter(s => s.fn.id === this.selectedFunctionId);
  }

  private visibleItems(): BudgetItem[] {
    return this.visibleSections.flatMap(s => s.items);
  }

  onBlankCountChange(): void {
    const n = Math.floor(Number(this.blankCount) || 0);
    this.blankCount = Math.min(Math.max(n, 0), MAX_BLANK_COLUMNS);
  }

  blankColumns(): string[] {
    return this.blankLabels.slice(0, this.blankCount);
  }

  trackByIndex(index: number): number {
    return index;
  }

  private itemValue(item: BudgetItem, field: AmountField): number {
    if (field === 'estimated') { return this.estimated(item); }
    if (field === 'actual') { return item.actualAmount ?? 0; }
    if (field === 'paid') { return item.paidAmount ?? 0; }
    return this.remaining(item);
  }

  sectionTotal(section: FunctionSection, field: AmountField): number {
    return section.items.reduce((sum, i) => sum + this.itemValue(i, field), 0);
  }

  grandTotal(field: AmountField): number {
    return this.visibleItems().reduce((sum, i) => sum + this.itemValue(i, field), 0);
  }

  splitsText(item: BudgetItem): string {
    return (item.splits ?? [])
      .filter(s => (s.label ?? '').trim().length > 0)
      .map(s => `${s.label} ₹${s.amount ?? 0}`)
      .join(', ');
  }

  print(): void {
    window.print();
  }
}
