import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { IncomeService } from '../../services/income.service';
import { IncomeItem } from '../../models/income-item.model';
import { TranslatePipe } from '../../pipes/translate.pipe';
import { TranslateService } from '../../services/translate.service';

@Component({
  selector: 'app-income',
  standalone: true,
  imports: [CommonModule, FormsModule, TranslatePipe],
  templateUrl: './income.component.html',
  styleUrl: './income.component.css'
})
export class IncomeComponent implements OnInit {

  items: IncomeItem[] = [];
  isLoading = true;

  editing: IncomeItem | null = null;
  editingId: number | null = null;
  deleteConfirmId: number | null = null;

  feedbackMessage = '';
  feedbackType: 'success' | 'error' = 'success';

  constructor(
    private incomeService: IncomeService,
    private translateService: TranslateService
  ) {}

  ngOnInit(): void { this.loadAll(); }

  loadAll(): void {
    this.isLoading = true;
    this.editing = null;
    this.deleteConfirmId = null;
    this.incomeService.getAll().subscribe({
      next: (items) => { this.items = items; this.isLoading = false; },
      error: () => { this.isLoading = false; }
    });
  }

  totalEstimated(): number { return this.items.reduce((s, i) => s + (i.estimatedAmount ?? 0), 0); }
  totalReceived(): number { return this.items.reduce((s, i) => s + (i.actualAmount ?? 0), 0); }
  totalPending(): number { return this.totalEstimated() - this.totalReceived(); }

  startAdd(): void {
    this.editing = { source: '', incomeDate: '', estimatedAmount: 0, actualAmount: null, notes: '' };
    this.editingId = null;
    this.deleteConfirmId = null;
  }

  startEdit(item: IncomeItem): void {
    this.editing = { ...item, incomeDate: item.incomeDate ?? '' };
    this.editingId = item.id ?? null;
    this.deleteConfirmId = null;
  }

  cancelEdit(): void { this.editing = null; this.editingId = null; }

  save(): void {
    if (!this.editing || !this.editing.source.trim()) { return; }
    const e = this.editing;
    const payload: IncomeItem = {
      ...e,
      source: e.source.trim(),
      incomeDate: e.incomeDate || null,
      estimatedAmount: e.estimatedAmount || 0,
      actualAmount: e.actualAmount === null || e.actualAmount === undefined || (e.actualAmount as any) === ''
        ? null : Number(e.actualAmount)
    };
    const call = this.editingId != null ? this.incomeService.update(payload) : this.incomeService.add(payload);
    call.subscribe({
      next: (resp) => {
        this.showFeedback(resp.status === 'SUCCESS' ? 'income.saveSuccess' : 'income.saveError',
          resp.status === 'SUCCESS' ? 'success' : 'error');
        if (resp.status === 'SUCCESS') { this.loadAll(); }
      },
      error: () => { this.showFeedback('income.saveError', 'error'); }
    });
  }

  requestDelete(id: number): void { this.deleteConfirmId = id; this.editing = null; }
  cancelDelete(): void { this.deleteConfirmId = null; }

  confirmDelete(id: number): void {
    this.incomeService.delete(id).subscribe({
      next: (resp) => {
        this.showFeedback(resp.status === 'SUCCESS' ? 'income.deleteSuccess' : 'income.deleteError',
          resp.status === 'SUCCESS' ? 'success' : 'error');
        this.loadAll();
      },
      error: () => { this.showFeedback('income.deleteError', 'error'); }
    });
  }

  private showFeedback(key: string, type: 'success' | 'error'): void {
    this.feedbackMessage = this.translateService.translate(key);
    this.feedbackType = type;
    setTimeout(() => { this.feedbackMessage = ''; }, 3000);
  }
}
