import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Subject, Subscription, forkJoin } from 'rxjs';
import { debounceTime, switchMap } from 'rxjs/operators';
import { PeopleService } from '../../services/people.service';
import { FunctionService } from '../../services/function.service';
import { GuestGroupService } from '../../services/guest-group.service';
import { GiftService } from '../../services/gift.service';
import { People } from '../../models/people.model';
import { Gift, GiftType } from '../../models/gift.model';
import { TmsFunction } from '../../models/function.model';
import { GuestGroup } from '../../models/guest-group.model';
import { TranslatePipe } from '../../pipes/translate.pipe';
import { TranslateService } from '../../services/translate.service';
import { TransliterationService } from '../../services/transliteration.service';
import { PersonFormComponent } from '../person-form/person-form.component';

@Component({
  selector: 'app-people-list',
  standalone: true,
  imports: [CommonModule, FormsModule, TranslatePipe, PersonFormComponent],
  templateUrl: './people-list.component.html',
  styleUrl: './people-list.component.css'
})
export class PeopleListComponent implements OnInit, OnDestroy {

  allPeople: People[] = [];
  functions: TmsFunction[] = [];
  groups: GuestGroup[] = [];
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

  // Gift entry state
  gifts: Gift[] = [];
  giftPersonId: number | null = null;
  giftDraft: { functionId: number | null; giftType: GiftType; value: number | null; description: string; notes: string } =
    { functionId: null, giftType: 'CASH', value: null, description: '', notes: '' };
  isSavingGift = false;

  private searchInput$ = new Subject<string>();
  private subs = new Subscription();

  constructor(
    private peopleService: PeopleService,
    private functionService: FunctionService,
    private guestGroupService: GuestGroupService,
    private giftService: GiftService,
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
    this.giftPersonId = null;

    this.guestGroupService.getAllGroups().subscribe({
      next: (groups) => { this.groups = groups; }
    });

    this.giftService.getAll().subscribe({
      next: (gifts) => { this.gifts = gifts; }
    });

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
    this.giftPersonId = null;
  }

  setSubFilter(sf: 'NONE' | 'IN' | 'NOT_IN' | 'INVITED' | 'YET_TO_INVITE'): void {
    this.subFilter = sf;
    this.editPersonId = null;
    this.deleteConfirmId = null;
    this.quickMarkPersonId = null;
    this.giftPersonId = null;
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
    this.giftPersonId = null;
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
    this.giftPersonId = null;
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
    this.giftPersonId = null;
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

  /** Gifts for a person, narrowed to the active function when a specific function tab is selected. */
  giftsFor(person: People): Gift[] {
    return this.gifts.filter(g =>
      g.personId === person.id &&
      (typeof this.activeFilter !== 'number' || g.functionId === this.activeFilter)
    );
  }

  /** Short label for a gift tag, e.g. "₹5,000 Cash" or "Gold clock". */
  giftLabel(gift: Gift): string {
    const value = gift.value != null ? '₹' + gift.value : '';
    if (gift.giftType === 'CASH') {
      return (value || this.translateService.translate('peopleList.cash')).trim();
    }
    const desc = gift.description?.trim() || this.translateService.translate('peopleList.item');
    return value ? `${desc} · ${value}` : desc;
  }

  /** The functions a person is on — options for the gift function dropdown on the "All" tab. */
  personFunctions(person: People): TmsFunction[] {
    return this.functions.filter(fn => person.invitedFunctionIds.includes(fn.id));
  }

  startGift(person: People): void {
    this.giftPersonId = person.id!;
    this.editPersonId = null;
    this.deleteConfirmId = null;
    this.quickMarkPersonId = null;
    // Pre-fill the function from the active tab, else fall back to the person's first function.
    const fallback = this.personFunctions(person)[0]?.id ?? null;
    this.giftDraft = {
      functionId: typeof this.activeFilter === 'number' ? this.activeFilter : fallback,
      giftType: 'CASH',
      value: null,
      description: '',
      notes: ''
    };
  }

  cancelGift(): void {
    this.giftPersonId = null;
  }

  /** Validity mirrors the backend rules: function required; value for cash, description for item. */
  canSaveGift(): boolean {
    const d = this.giftDraft;
    if (d.functionId == null) { return false; }
    if (d.giftType === 'CASH') { return d.value != null && (d.value as any) !== ''; }
    return !!d.description.trim();
  }

  saveGift(person: People): void {
    if (!this.canSaveGift() || this.isSavingGift) { return; }
    this.isSavingGift = true;
    const d = this.giftDraft;
    const payload: Gift = {
      personId: person.id!,
      functionId: d.functionId!,
      giftType: d.giftType,
      value: d.value === null || (d.value as any) === '' ? null : Number(d.value),
      description: d.description.trim(),
      notes: d.notes.trim()
    };
    this.giftService.add(payload).subscribe({
      next: (resp) => {
        this.isSavingGift = false;
        this.feedbackMessage = resp.status === 'SUCCESS'
          ? `${person.name} ${this.translateService.translate('peopleList.giftSaved')}`
          : this.translateService.translate('peopleList.giftSaveFailed');
        if (resp.status === 'SUCCESS') { this.loadAll(); }
        setTimeout(() => { this.feedbackMessage = ''; }, 3000);
      },
      error: () => {
        this.isSavingGift = false;
        this.feedbackMessage = this.translateService.translate('peopleList.giftSaveFailed');
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
