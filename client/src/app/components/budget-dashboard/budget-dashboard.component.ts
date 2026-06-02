import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { forkJoin } from 'rxjs';
import { BudgetService } from '../../services/budget.service';
import { IncomeService } from '../../services/income.service';
import { FunctionService } from '../../services/function.service';
import { BudgetItem, budgetEstimated, budgetRemaining } from '../../models/budget-item.model';
import { IncomeItem } from '../../models/income-item.model';
import { TmsFunction } from '../../models/function.model';
import { TranslatePipe } from '../../pipes/translate.pipe';

interface FunctionRollup {
  fn: TmsFunction;
  estimated: number;
  paid: number;
  remaining: number;
  overspent: boolean;
  paidPct: number;
}

@Component({
  selector: 'app-budget-dashboard',
  standalone: true,
  imports: [CommonModule, TranslatePipe],
  templateUrl: './budget-dashboard.component.html',
  styleUrl: './budget-dashboard.component.css'
})
export class BudgetDashboardComponent implements OnInit {

  functions: TmsFunction[] = [];
  items: BudgetItem[] = [];
  income: IncomeItem[] = [];
  isLoading = true;

  rollups: FunctionRollup[] = [];
  topExpenses: BudgetItem[] = [];

  ngOnInit(): void {
    forkJoin({
      fns: this.functionService.getAllFunctions(),
      items: this.budgetService.getAll(),
      income: this.incomeService.getAll()
    }).subscribe({
      next: ({ fns, items, income }) => {
        this.functions = fns;
        this.items = items;
        this.income = income;
        this.compute();
        this.isLoading = false;
      },
      error: () => { this.isLoading = false; }
    });
  }

  constructor(
    private budgetService: BudgetService,
    private incomeService: IncomeService,
    private functionService: FunctionService
  ) {}

  private compute(): void {
    this.rollups = this.functions.map(fn => {
      const its = this.items.filter(i => i.functionId === fn.id);
      const estimated = its.reduce((s, i) => s + budgetEstimated(i), 0);
      const paid = its.reduce((s, i) => s + (i.paidAmount ?? 0), 0);
      const remaining = its.reduce((s, i) => s + budgetRemaining(i), 0);
      const overspent = its.some(i => i.actualAmount != null && i.actualAmount > budgetEstimated(i));
      return { fn, estimated, paid, remaining, overspent, paidPct: estimated > 0 ? Math.round((paid / estimated) * 100) : 0 };
    });
    this.topExpenses = [...this.items]
      .sort((a, b) => budgetEstimated(b) - budgetEstimated(a))
      .slice(0, 5);
  }

  est(i: BudgetItem): number { return budgetEstimated(i); }

  // ── Expense totals ──
  get totalEstimated(): number { return this.items.reduce((s, i) => s + budgetEstimated(i), 0); }
  get totalPaid(): number { return this.items.reduce((s, i) => s + (i.paidAmount ?? 0), 0); }
  get totalRemaining(): number { return this.items.reduce((s, i) => s + budgetRemaining(i), 0); }
  get paidPct(): number { return this.totalEstimated > 0 ? Math.round((this.totalPaid / this.totalEstimated) * 100) : 0; }

  // ── Income totals ──
  get incomeEstimated(): number { return this.income.reduce((s, i) => s + (i.estimatedAmount ?? 0), 0); }
  get incomeReceived(): number { return this.income.reduce((s, i) => s + (i.actualAmount ?? 0), 0); }

  // ── Balance ──
  get cashPosition(): number { return this.incomeReceived - this.totalPaid; }        // money in hand vs paid out
  get plannedBalance(): number { return this.incomeEstimated - this.totalEstimated; } // planned income vs planned spend

  functionName(fn: TmsFunction): string { return fn.name; }
}
