import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Subject, Subscription } from 'rxjs';
import { debounceTime, switchMap } from 'rxjs/operators';
import { PeopleService } from '../../services/people.service';
import { FunctionService } from '../../services/function.service';
import { People } from '../../models/people.model';
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
  searchQuery = '';
  searchTranslitSuggestions: string[] = [];

  editPersonId: number | null = null;
  deleteConfirmId: number | null = null;
  feedbackMessage = '';

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
    let result: People[];
    if (this.activeFilter === 'ALL') {
      result = this.allPeople;
    } else {
      // Function tab: show people NOT yet on this function's invitation list
      result = this.allPeople.filter(p => !p.invitedFunctionIds.includes(this.activeFilter as number));
    }

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
