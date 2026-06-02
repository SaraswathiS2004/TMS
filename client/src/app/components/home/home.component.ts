import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { FunctionService } from '../../services/function.service';
import { PeopleService } from '../../services/people.service';
import { TmsFunction } from '../../models/function.model';
import { People } from '../../models/people.model';
import { TranslatePipe } from '../../pipes/translate.pipe';
import { TranslateService } from '../../services/translate.service';

@Component({
  selector: 'app-home',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule, TranslatePipe],
  templateUrl: './home.component.html',
  styleUrl: './home.component.css'
})
export class HomeComponent implements OnInit {

  functions: TmsFunction[] = [];
  people: People[] = [];
  isLoading = true;

  showAddForm = false;
  newName = '';
  newColor = '#4f46e5';
  newDate = '';
  nameError = false;
  isSaving = false;

  editId: number | null = null;
  editData: { name: string; color: string; eventDate: string; displayOrder: number } =
    { name: '', color: '#4f46e5', eventDate: '', displayOrder: 0 };
  deleteConfirmId: number | null = null;

  feedbackMessage = '';
  feedbackType: 'success' | 'error' = 'success';

  constructor(
    private functionService: FunctionService,
    private peopleService: PeopleService,
    private translateService: TranslateService
  ) {}

  ngOnInit(): void {
    this.loadAll();
  }

  loadAll(): void {
    this.isLoading = true;
    this.editId = null;
    this.deleteConfirmId = null;
    this.functionService.getAllFunctions().subscribe({
      next: (fns) => {
        this.functions = this.sortByDate(fns);
        this.peopleService.getAllPeople().subscribe({
          next: (people) => { this.people = people; this.isLoading = false; },
          error: () => { this.isLoading = false; }
        });
      },
      error: () => { this.isLoading = false; }
    });
  }

  private sortByDate(fns: TmsFunction[]): TmsFunction[] {
    return [...fns].sort((a, b) => {
      if (a.eventDate && b.eventDate) { return a.eventDate.localeCompare(b.eventDate); }
      if (a.eventDate) { return -1; }
      if (b.eventDate) { return 1; }
      return a.displayOrder - b.displayOrder;
    });
  }

  guestCount(fnId: number): number {
    return this.people.filter(p => p.invitedFunctionIds.includes(fnId)).length;
  }

  expectedCount(fnId: number): number {
    return this.people
      .filter(p => p.invitedFunctionIds.includes(fnId))
      .reduce((sum, p) => sum + p.numberOfPerson, 0);
  }

  daysUntil(dateStr?: string | null): number | null {
    if (!dateStr) { return null; }
    const today = new Date();
    today.setHours(0, 0, 0, 0);
    const target = new Date(dateStr + 'T00:00:00');
    if (isNaN(target.getTime())) { return null; }
    return Math.round((target.getTime() - today.getTime()) / 86400000);
  }

  // ── Add ──
  toggleAddForm(): void {
    this.showAddForm = !this.showAddForm;
    this.nameError = false;
  }

  saveFunction(): void {
    if (!this.newName.trim()) { this.nameError = true; return; }
    this.nameError = false;
    this.isSaving = true;
    this.functionService.addFunction({
      name: this.newName.trim(),
      color: this.newColor,
      eventDate: this.newDate || null,
      displayOrder: this.functions.length
    }).subscribe({
      next: (resp) => {
        this.showFeedback(resp.status === 'SUCCESS' ? 'home.addSuccess' : 'home.saveError',
          resp.status === 'SUCCESS' ? 'success' : 'error');
        if (resp.status === 'SUCCESS') {
          this.newName = ''; this.newColor = '#4f46e5'; this.newDate = ''; this.showAddForm = false;
          this.loadAll();
        }
        this.isSaving = false;
      },
      error: () => { this.showFeedback('home.saveError', 'error'); this.isSaving = false; }
    });
  }

  // ── Edit ──
  startEdit(fn: TmsFunction): void {
    this.editId = fn.id;
    this.editData = { name: fn.name, color: fn.color, eventDate: fn.eventDate ?? '', displayOrder: fn.displayOrder };
    this.deleteConfirmId = null;
  }

  cancelEdit(): void { this.editId = null; }

  saveEdit(): void {
    if (this.editId === null || !this.editData.name.trim()) { return; }
    this.functionService.updateFunction({
      id: this.editId,
      name: this.editData.name.trim(),
      color: this.editData.color,
      eventDate: this.editData.eventDate || null,
      displayOrder: this.editData.displayOrder
    }).subscribe({
      next: (resp) => {
        this.showFeedback(resp.status === 'SUCCESS' ? 'home.editSuccess' : 'home.saveError',
          resp.status === 'SUCCESS' ? 'success' : 'error');
        if (resp.status === 'SUCCESS') { this.loadAll(); }
      },
      error: () => { this.showFeedback('home.saveError', 'error'); }
    });
  }

  // ── Delete ──
  requestDelete(id: number): void { this.deleteConfirmId = id; this.editId = null; }
  cancelDelete(): void { this.deleteConfirmId = null; }

  confirmDelete(id: number): void {
    this.functionService.deleteFunction(id).subscribe({
      next: (resp) => {
        this.showFeedback(resp.status === 'SUCCESS' ? 'home.deleteSuccess' : 'home.deleteError',
          resp.status === 'SUCCESS' ? 'success' : 'error');
        this.loadAll();
      },
      error: () => { this.showFeedback('home.deleteError', 'error'); }
    });
  }

  private showFeedback(key: string, type: 'success' | 'error'): void {
    this.feedbackMessage = this.translateService.translate(key);
    this.feedbackType = type;
    setTimeout(() => { this.feedbackMessage = ''; }, 3000);
  }
}
