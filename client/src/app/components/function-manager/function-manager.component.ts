import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { FunctionService } from '../../services/function.service';
import { PeopleService } from '../../services/people.service';
import { TmsFunction } from '../../models/function.model';
import { People } from '../../models/people.model';
import { TranslatePipe } from '../../pipes/translate.pipe';

interface EditFnData {
  name: string;
  color: string;
  displayOrder: number;
}

@Component({
  selector: 'app-function-manager',
  standalone: true,
  imports: [CommonModule, FormsModule, TranslatePipe],
  templateUrl: './function-manager.component.html',
  styleUrl: './function-manager.component.css'
})
export class FunctionManagerComponent implements OnInit {

  functions: TmsFunction[] = [];
  people: People[] = [];
  isLoading = true;

  newName = '';
  newColor = '#4f46e5';
  isSaving = false;
  nameError = false;

  editFunctionId: number | null = null;
  editData: EditFnData = { name: '', color: '#4f46e5', displayOrder: 0 };
  isEditSaving = false;

  deleteConfirmId: number | null = null;
  feedbackMessage = '';
  feedbackType: 'success' | 'error' = 'success';

  constructor(
    private functionService: FunctionService,
    private peopleService: PeopleService
  ) {}

  ngOnInit(): void {
    this.loadAll();
  }

  loadAll(): void {
    this.isLoading = true;
    this.functionService.getAllFunctions().subscribe({
      next: (fns) => {
        this.functions = fns;
        this.peopleService.getAllPeople().subscribe({
          next: (people) => {
            this.people = people;
            this.isLoading = false;
          },
          error: () => { this.isLoading = false; }
        });
      },
      error: () => { this.isLoading = false; }
    });
  }

  invitedCount(fn: TmsFunction): number {
    return this.people.filter(p => p.invitedFunctionIds.includes(fn.id)).length;
  }

  invitedStatusCount(fn: TmsFunction): number {
    return this.people.filter(p =>
      p.functionStatuses?.[String(fn.id)] === 'INVITED'
    ).length;
  }

  saveFunction(): void {
    if (!this.newName.trim()) {
      this.nameError = true;
      return;
    }
    this.nameError = false;
    this.isSaving = true;

    this.functionService.addFunction({ name: this.newName.trim(), color: this.newColor, displayOrder: this.functions.length }).subscribe({
      next: (resp) => {
        if (resp.status === 'SUCCESS') {
          this.showFeedback('functions.addSuccess', 'success');
          this.newName = '';
          this.newColor = '#4f46e5';
          this.loadAll();
        } else {
          this.showFeedback('functions.deleteError', 'error');
        }
        this.isSaving = false;
      },
      error: () => {
        this.showFeedback('functions.deleteError', 'error');
        this.isSaving = false;
      }
    });
  }

  startEditFunction(fn: TmsFunction): void {
    this.editFunctionId = fn.id;
    this.editData = { name: fn.name, color: fn.color, displayOrder: fn.displayOrder };
    this.deleteConfirmId = null;
  }

  cancelEditFunction(): void {
    this.editFunctionId = null;
  }

  saveEditFunction(): void {
    if (!this.editFunctionId || !this.editData.name.trim()) { return; }
    this.isEditSaving = true;
    const fn: TmsFunction = {
      id: this.editFunctionId,
      name: this.editData.name.trim(),
      color: this.editData.color,
      displayOrder: this.editData.displayOrder
    };
    this.functionService.updateFunction(fn).subscribe({
      next: (resp) => {
        if (resp.status === 'SUCCESS') {
          this.showFeedback('functions.editSuccess', 'success');
          this.editFunctionId = null;
          this.loadAll();
        } else {
          this.showFeedback('functions.editError', 'error');
        }
        this.isEditSaving = false;
      },
      error: () => {
        this.showFeedback('functions.editError', 'error');
        this.isEditSaving = false;
      }
    });
  }

  requestDelete(id: number): void {
    this.deleteConfirmId = id;
    this.editFunctionId = null;
  }

  cancelDelete(): void {
    this.deleteConfirmId = null;
  }

  confirmDelete(id: number): void {
    this.functionService.deleteFunction(id).subscribe({
      next: (resp) => {
        if (resp.status === 'SUCCESS') {
          this.showFeedback('functions.deleteSuccess', 'success');
          this.loadAll();
        } else {
          this.showFeedback('functions.deleteError', 'error');
        }
        this.deleteConfirmId = null;
      },
      error: () => {
        this.showFeedback('functions.deleteError', 'error');
        this.deleteConfirmId = null;
      }
    });
  }

  private showFeedback(key: string, type: 'success' | 'error'): void {
    this.feedbackMessage = key;
    this.feedbackType = type;
    setTimeout(() => { this.feedbackMessage = ''; }, 4000);
  }
}
