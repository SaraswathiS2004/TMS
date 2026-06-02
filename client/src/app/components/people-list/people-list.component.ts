import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Subject, Subscription, forkJoin } from 'rxjs';
import { debounceTime, switchMap } from 'rxjs/operators';
import { PeopleService } from '../../services/people.service';
import { FunctionService } from '../../services/function.service';
import { People, effectivePersonCount } from '../../models/people.model';
import { TmsFunction } from '../../models/function.model';
import { TranslatePipe } from '../../pipes/translate.pipe';
import { TranslateService } from '../../services/translate.service';
import { TransliterationService } from '../../services/transliteration.service';
import { PersonFormComponent } from '../person-form/person-form.component';

@Component({
  selector: 'app-people-list',
  standalone: true,
  imports: [CommonModule, TranslatePipe, PersonFormComponent],
  templateUrl: './people-list.component.html',
  styleUrl: './people-list.component.css'
})
export class PeopleListComponent implements OnInit, OnDestroy {

  allPeople: People[] = [];
  functions: TmsFunction[] = [];
  isLoading = true;

  activeFilter: 'ALL' | number = 'ALL';
  subFilter: 'NONE' | 'IN' | 'NOT_IN' | 'INVITED' | 'YET_TO_INVITE' = 'NONE';
  searchQuery = '';
  searchTranslitSuggestions: string[] = [];

  editPersonId: number | null = null;
  deleteConfirmId: number | null = null;
  feedbackMessage = '';

  // Quick "mark as invited" popup state
  quickMarkPersonId: number | null = null;
  quickMarkSelectedIds: number[] = [];
  isQuickMarking = false;

  private searchInput$ = new Subject<string>();
  private subs = new Subscription();

  constructor(
    private peopleService: PeopleService,
    private functionService: FunctionService,
    private translateService: TranslateService,
    private transliterationService: TransliterationService
  ) {}

  ngOnInit(): void {
    this.loadAll();

    this.subs.add(
      this.searchInput$.pipe(
        debounceTime(300),
        switchMap(text => this.transliterationService.getSuggestions(text))
      ).subscribe(items => { this.searchTranslitSuggestions = items; })
    );
  }

  ngOnDestroy(): void {
    this.subs.unsubscribe();
  }

  loadAll(): void {
    this.isLoading = true;
    this.editPersonId = null;
    this.deleteConfirmId = null;
    this.quickMarkPersonId = null;

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
    let result = this.applyFilters(this.allPeople);
    const q = this.searchQuery.trim().toLowerCase();
    if (q) {
      result = result.filter(p => {
        const name = p.name.toLowerCase();
        const city = p.city.toLowerCase();
        if (name.includes(q) || city.includes(q)) { return true; }
        return this.searchTranslitSuggestions.some(s => {
          const sl = s.toLowerCase();
          return name.includes(sl) || city.includes(sl);
        });
      });
    }
    return result;
  }

  private applyFilters(people: People[]): People[] {
    if (this.activeFilter === 'ALL') {
      if (this.subFilter === 'INVITED') {
        return people.filter(p => Object.values(p.functionStatuses ?? {}).some(s => s === 'INVITED'));
      }
      if (this.subFilter === 'YET_TO_INVITE') {
        return people.filter(p =>
          p.invitedFunctionIds.length > 0 &&
          !Object.values(p.functionStatuses ?? {}).some(s => s === 'INVITED')
        );
      }
      return people;
    }
    const fnId = this.activeFilter as number;
    const fnIdStr = String(fnId);
    return people.filter(p => {
      const isIn = p.invitedFunctionIds.includes(fnId);
      const isInvited = p.functionStatuses?.[fnIdStr] === 'INVITED';
      if (this.subFilter === 'IN') { return isIn; }
      if (this.subFilter === 'NOT_IN') { return !isIn; }
      if (this.subFilter === 'INVITED') { return isIn && isInvited; }
      if (this.subFilter === 'YET_TO_INVITE') { return isIn && !isInvited; }
      return true;
    });
  }

  get emptyStateKey(): string {
    if (this.isSearchActive) { return 'peopleList.searchNoMatch'; }
    if (this.activeFilter === 'ALL') {
      if (this.subFilter === 'INVITED') { return 'peopleList.emptyAllInvited'; }
      if (this.subFilter === 'YET_TO_INVITE') { return 'peopleList.emptyAllYetToInvite'; }
      return 'peopleList.noResults';
    }
    if (this.subFilter === 'NOT_IN') { return 'peopleList.emptyFnNotIn'; }
    if (this.subFilter === 'INVITED') { return 'peopleList.emptyFnInvited'; }
    if (this.subFilter === 'YET_TO_INVITE') { return 'peopleList.emptyFnYetToInvite'; }
    return 'peopleList.emptyFnIn';
  }

  get isSearchActive(): boolean {
    return this.searchQuery.trim().length > 0;
  }

  onSearch(event: Event): void {
    const val = (event.target as HTMLInputElement).value;
    this.searchQuery = val;
    this.searchTranslitSuggestions = [];
    if (val.trim()) { this.searchInput$.next(val.trim()); }
  }

  clearSearch(): void {
    this.searchQuery = '';
    this.searchTranslitSuggestions = [];
  }

  setFilter(f: 'ALL' | number): void {
    this.activeFilter = f;
    this.subFilter = f === 'ALL' ? 'NONE' : 'IN';
    this.editPersonId = null;
    this.deleteConfirmId = null;
    this.quickMarkPersonId = null;
  }

  setSubFilter(sf: 'NONE' | 'IN' | 'NOT_IN' | 'INVITED' | 'YET_TO_INVITE'): void {
    this.subFilter = sf;
    this.editPersonId = null;
    this.deleteConfirmId = null;
    this.quickMarkPersonId = null;
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
    this.quickMarkPersonId = null;
  }

  cancelEditPerson(): void {
    this.editPersonId = null;
  }

  /** Functions the person is on but not yet marked as invited — the ones a quick-mark can act on. */
  pendingFunctions(person: People): TmsFunction[] {
    return this.functions.filter(fn =>
      person.invitedFunctionIds.includes(fn.id) &&
      person.functionStatuses?.[String(fn.id)] !== 'INVITED'
    );
  }

  hasPendingFunctions(person: People): boolean {
    return this.pendingFunctions(person).length > 0;
  }

  startQuickMark(person: People): void {
    this.quickMarkPersonId = person.id!;
    this.editPersonId = null;
    this.deleteConfirmId = null;
    // Pre-select the function currently being browsed, if the person still needs it.
    const pendingIds = this.pendingFunctions(person).map(fn => fn.id);
    this.quickMarkSelectedIds =
      typeof this.activeFilter === 'number' && pendingIds.includes(this.activeFilter)
        ? [this.activeFilter]
        : [];
  }

  cancelQuickMark(): void {
    this.quickMarkPersonId = null;
    this.quickMarkSelectedIds = [];
  }

  isQuickMarkSelected(fnId: number): boolean {
    return this.quickMarkSelectedIds.includes(fnId);
  }

  toggleQuickMarkFn(fnId: number): void {
    if (this.quickMarkSelectedIds.includes(fnId)) {
      this.quickMarkSelectedIds = this.quickMarkSelectedIds.filter(id => id !== fnId);
    } else {
      this.quickMarkSelectedIds = [...this.quickMarkSelectedIds, fnId];
    }
  }

  confirmQuickMark(person: People): void {
    if (this.quickMarkSelectedIds.length === 0 || this.isQuickMarking) { return; }
    this.isQuickMarking = true;
    const calls = this.quickMarkSelectedIds.map(fnId =>
      this.peopleService.updateFunctionStatus(person.id!, fnId, 'INVITED')
    );
    forkJoin(calls).subscribe({
      next: (responses) => {
        this.isQuickMarking = false;
        const allOk = responses.every(r => r.status === 'SUCCESS');
        this.feedbackMessage = allOk
          ? `${person.name} ${this.translateService.translate('peopleList.quickMarkDone')}`
          : this.translateService.translate('peopleList.updateFailed');
        this.loadAll();
        setTimeout(() => { this.feedbackMessage = ''; }, 3000);
      },
      error: () => {
        this.isQuickMarking = false;
        this.feedbackMessage = this.translateService.translate('peopleList.updateFailed');
        setTimeout(() => { this.feedbackMessage = ''; }, 3000);
      }
    });
  }

  onPersonSaved(): void {
    this.feedbackMessage = this.translateService.translate('peopleList.savePersonSuccess');
    this.loadAll();
    setTimeout(() => { this.feedbackMessage = ''; }, 3000);
  }

  requestDelete(id: number): void {
    this.deleteConfirmId = id;
    this.editPersonId = null;
    this.quickMarkPersonId = null;
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

  effectiveCount(person: People): number {
    return effectivePersonCount(person);
  }

  namePersonsSummary(person: People): string {
    return (person.persons ?? [])
      .map(p => p.note ? `${p.name} (${p.note})` : p.name)
      .join(', ');
  }
}
