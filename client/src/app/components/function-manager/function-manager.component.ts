import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Subject, Subscription } from 'rxjs';
import { debounceTime, switchMap } from 'rxjs/operators';
import { FunctionService } from '../../services/function.service';
import { PeopleService } from '../../services/people.service';
import { TmsFunction } from '../../models/function.model';
import { People } from '../../models/people.model';
import { TranslatePipe } from '../../pipes/translate.pipe';
import { TranslateService } from '../../services/translate.service';
import { TransliterationService } from '../../services/transliteration.service';

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
export class FunctionManagerComponent implements OnInit, OnDestroy {

  functions: TmsFunction[] = [];
  people: People[] = [];
  isLoading = true;

  newName = '';
  newColor = '#4f46e5';
  isSaving = false;
  nameError = false;
  newNameTSuggestions: string[] = [];

  editFunctionId: number | null = null;
  editData: EditFnData = { name: '', color: '#4f46e5', displayOrder: 0 };
  isEditSaving = false;
  editNameTSuggestions: string[] = [];

  deleteConfirmId: number | null = null;
  feedbackMessage = '';
  feedbackType: 'success' | 'error' = 'success';

  private newNameInput$ = new Subject<string>();
  private editNameInput$ = new Subject<string>();
  private subs = new Subscription();

  get isTamilMode(): boolean {
    return this.translateService.currentLang === 'ta';
  }

  constructor(
    private functionService: FunctionService,
    private peopleService: PeopleService,
    private translateService: TranslateService,
    private transliterationService: TransliterationService
  ) {}

  ngOnInit(): void {
    this.loadAll();

    this.subs.add(
      this.newNameInput$.pipe(
        debounceTime(300),
        switchMap(text => this.transliterationService.getSuggestions(text))
      ).subscribe(items => { this.newNameTSuggestions = items; })
    );

    this.subs.add(
      this.editNameInput$.pipe(
        debounceTime(300),
        switchMap(text => this.transliterationService.getSuggestions(text))
      ).subscribe(items => { this.editNameTSuggestions = items; })
    );
  }

  ngOnDestroy(): void {
    this.subs.unsubscribe();
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

  onNewNameInput(event: Event): void {
    const val = (event.target as HTMLInputElement).value;
    this.newName = val;
    this.nameError = false;
    if (this.isTamilMode && val.trim()) {
      this.newNameInput$.next(val);
    } else {
      this.newNameTSuggestions = [];
    }
  }

  selectNewName(name: string): void {
    this.newName = name;
    this.newNameTSuggestions = [];
  }

  hideNewNameSuggestions(): void {
    setTimeout(() => { this.newNameTSuggestions = []; }, 150);
  }

  onEditNameInput(event: Event): void {
    const val = (event.target as HTMLInputElement).value;
    this.editData = { ...this.editData, name: val };
    if (this.isTamilMode && val.trim()) {
      this.editNameInput$.next(val);
    } else {
      this.editNameTSuggestions = [];
    }
  }

  selectEditName(name: string): void {
    this.editData = { ...this.editData, name };
    this.editNameTSuggestions = [];
  }

  hideEditNameSuggestions(): void {
    setTimeout(() => { this.editNameTSuggestions = []; }, 150);
  }

  saveFunction(): void {
    if (!this.newName.trim()) {
      this.nameError = true;
      return;
    }
    this.nameError = false;
    this.isSaving = true;
    this.newNameTSuggestions = [];

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
    this.editNameTSuggestions = [];
    this.deleteConfirmId = null;
  }

  cancelEditFunction(): void {
    this.editFunctionId = null;
    this.editNameTSuggestions = [];
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
