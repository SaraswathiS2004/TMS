import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { PeopleService } from '../../services/people.service';
import { FunctionService } from '../../services/function.service';
import { People } from '../../models/people.model';
import { TmsFunction } from '../../models/function.model';
import { TranslatePipe } from '../../pipes/translate.pipe';
import { TranslateService } from '../../services/translate.service';

@Component({
  selector: 'app-people-list',
  standalone: true,
  imports: [CommonModule, TranslatePipe],
  templateUrl: './people-list.component.html',
  styleUrl: './people-list.component.css'
})
export class PeopleListComponent implements OnInit {

  allPeople: People[] = [];
  functions: TmsFunction[] = [];
  isLoading = true;

  // 'ALL' | 'NONE' | number (functionId)
  activeFilter: 'ALL' | 'NONE' | number = 'NONE';

  editingId: number | null = null;
  pendingFunctionIds: number[] = [];
  isSaving = false;

  deleteConfirmId: number | null = null;
  feedbackMessage = '';

  constructor(
    private peopleService: PeopleService,
    private functionService: FunctionService,
    private translateService: TranslateService
  ) {}

  ngOnInit(): void {
    this.loadAll();
  }

  loadAll(): void {
    this.isLoading = true;
    this.editingId = null;
    this.deleteConfirmId = null;

    this.functionService.getAllFunctions().subscribe({
      next: (fns) => {
        this.functions = fns;
        this.peopleService.getAllPeople().subscribe({
          next: (people) => {
            this.allPeople = people;
            this.isLoading = false;
          },
          error: () => { this.isLoading = false; }
        });
      },
      error: () => { this.isLoading = false; }
    });
  }

  get filteredPeople(): People[] {
    if (this.activeFilter === 'ALL') { return this.allPeople; }
    if (this.activeFilter === 'NONE') { return this.allPeople.filter(p => p.invitedFunctionIds.length === 0); }
    return this.allPeople.filter(p => p.invitedFunctionIds.includes(this.activeFilter as number));
  }

  setFilter(f: 'ALL' | 'NONE' | number): void {
    this.activeFilter = f;
    this.editingId = null;
    this.deleteConfirmId = null;
  }

  functionName(id: number): string {
    return this.functions.find(f => f.id === id)?.name ?? String(id);
  }

  functionColor(id: number): string {
    return this.functions.find(f => f.id === id)?.color ?? '#94a3b8';
  }

  // Edit invitations
  startEdit(person: People): void {
    this.editingId = person.id!;
    this.pendingFunctionIds = [...person.invitedFunctionIds];
    this.deleteConfirmId = null;
  }

  cancelEdit(): void {
    this.editingId = null;
  }

  togglePendingFunction(fnId: number): void {
    if (this.pendingFunctionIds.includes(fnId)) {
      this.pendingFunctionIds = this.pendingFunctionIds.filter(id => id !== fnId);
    } else {
      this.pendingFunctionIds = [...this.pendingFunctionIds, fnId];
    }
  }

  saveInvitations(person: People): void {
    if (!person.id) { return; }
    this.isSaving = true;

    this.peopleService.updateFunctionInvitations(person.id, this.pendingFunctionIds).subscribe({
      next: (resp) => {
        if (resp.status === 'SUCCESS') {
          this.feedbackMessage = `${person.name} — ${this.translateService.translate('peopleList.markedAs')}`;
          this.loadAll();
          setTimeout(() => { this.feedbackMessage = ''; }, 3000);
        } else {
          this.feedbackMessage = this.translateService.translate('peopleList.updateFailed');
          setTimeout(() => { this.feedbackMessage = ''; }, 3000);
        }
        this.isSaving = false;
        this.editingId = null;
      },
      error: () => {
        this.feedbackMessage = this.translateService.translate('peopleList.updateFailed');
        this.isSaving = false;
        setTimeout(() => { this.feedbackMessage = ''; }, 3000);
      }
    });
  }

  // Delete
  requestDelete(id: number): void {
    this.deleteConfirmId = id;
    this.editingId = null;
  }

  cancelDelete(): void {
    this.deleteConfirmId = null;
  }

  confirmDelete(person: People): void {
    if (!person.id) { return; }
    this.peopleService.deletePerson(person.id).subscribe({
      next: (resp) => {
        this.feedbackMessage = resp.status === 'SUCCESS'
          ? `${person.name} ${this.translateService.translate('peopleList.removedMsg')}`
          : this.translateService.translate('peopleList.deleteFailed');
        this.loadAll();
        setTimeout(() => { this.feedbackMessage = ''; }, 3000);
      },
      error: () => {
        this.feedbackMessage = this.translateService.translate('peopleList.deleteFailed');
        setTimeout(() => { this.feedbackMessage = ''; }, 3000);
      }
    });
  }

  relationBadgeClass(relation: string): string {
    const map: Record<string, string> = {
      CLOSE: 'badge-close', DISTANCE: 'badge-distance', FRIENDS: 'badge-friends'
    };
    return map[relation] ?? '';
  }
}
