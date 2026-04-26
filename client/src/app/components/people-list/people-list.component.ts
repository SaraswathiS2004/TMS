import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { PeopleService } from '../../services/people.service';
import { FunctionService } from '../../services/function.service';
import { People, RelationType } from '../../models/people.model';
import { TmsFunction } from '../../models/function.model';
import { TranslatePipe } from '../../pipes/translate.pipe';
import { TranslateService } from '../../services/translate.service';
import { PersonFormComponent } from '../person-form/person-form.component';

@Component({
  selector: 'app-people-list',
  standalone: true,
  imports: [CommonModule, TranslatePipe, PersonFormComponent],
  templateUrl: './people-list.component.html',
  styleUrl: './people-list.component.css'
})
export class PeopleListComponent implements OnInit {

  allPeople: People[] = [];
  functions: TmsFunction[] = [];
  isLoading = true;

  activeFilter: 'ALL' | 'NONE' | number = 'NONE';

  editPersonId: number | null = null;

  markingStatusPersonId: number | null = null;

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
    this.editPersonId = null;
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

  get isNumberFilter(): boolean {
    return typeof this.activeFilter === 'number';
  }

  get activeFunctionId(): number {
    return this.activeFilter as number;
  }

  getStatusForFunction(person: People, functionId: number): string {
    return person.functionStatuses?.[String(functionId)] ?? 'NOT_INVITED';
  }

  markInvited(person: People, functionId: number): void {
    if (!person.id) { return; }
    this.markingStatusPersonId = person.id;
    const currentStatus = this.getStatusForFunction(person, functionId);
    const newStatus = currentStatus === 'INVITED' ? 'NOT_INVITED' : 'INVITED';
    this.peopleService.updateFunctionStatus(person.id, functionId, newStatus).subscribe({
      next: (resp) => {
        if (resp.status === 'SUCCESS') {
          if (!person.functionStatuses) { person.functionStatuses = {}; }
          person.functionStatuses[String(functionId)] = newStatus;
        }
        this.markingStatusPersonId = null;
      },
      error: () => { this.markingStatusPersonId = null; }
    });
  }

  setFilter(f: 'ALL' | 'NONE' | number): void {
    this.activeFilter = f;
    this.editPersonId = null;
    this.deleteConfirmId = null;
  }

  functionName(id: number): string {
    return this.functions.find(f => f.id === id)?.name ?? String(id);
  }

  functionColor(id: number): string {
    return this.functions.find(f => f.id === id)?.color ?? '#94a3b8';
  }

  startEditPerson(person: People): void {
    this.editPersonId = person.id!;
    this.deleteConfirmId = null;
  }

  cancelEditPerson(): void {
    this.editPersonId = null;
  }

  onPersonSaved(): void {
    this.feedbackMessage = this.translateService.translate('peopleList.savePersonSuccess');
    this.loadAll();
    setTimeout(() => { this.feedbackMessage = ''; }, 3000);
  }

  requestDelete(id: number): void {
    this.deleteConfirmId = id;
    this.editPersonId = null;
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
      CLOSE_RELATIVE: 'badge-close', DISTANCE_RELATIVE: 'badge-distance', FRIENDS: 'badge-friends'
    };
    return map[relation] ?? '';
  }
}
