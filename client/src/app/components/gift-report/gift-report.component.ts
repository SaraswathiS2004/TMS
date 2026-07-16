import { Component, OnDestroy, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { forkJoin } from 'rxjs';
import { GiftService } from '../../services/gift.service';
import { PeopleService } from '../../services/people.service';
import { FunctionService } from '../../services/function.service';
import { Gift } from '../../models/gift.model';
import { People } from '../../models/people.model';
import { TmsFunction } from '../../models/function.model';
import { TranslatePipe } from '../../pipes/translate.pipe';
import { TranslateService } from '../../services/translate.service';

interface FunctionGifts {
  fn: TmsFunction;
  gifts: Gift[];
}

const MAX_BLANK_COLUMNS = 5;

@Component({
  selector: 'app-gift-report',
  standalone: true,
  imports: [CommonModule, FormsModule, TranslatePipe],
  templateUrl: './gift-report.component.html',
  styleUrl: './gift-report.component.css'
})
export class GiftReportComponent implements OnInit, OnDestroy {

  sections: FunctionGifts[] = [];
  gifts: Gift[] = [];
  isLoading = true;
  generatedOn = new Date();

  // 'ALL' or a function id — limits the report to one function
  selectedFunctionId: number | 'ALL' = 'ALL';

  // Field selection (guest name and # are always shown)
  showCity = true;
  showType = true;
  showDescription = true;
  showValue = true;
  showDate = true;
  showNotes = false;

  // Blank pen-fillable columns
  readonly maxBlankColumns = MAX_BLANK_COLUMNS;
  blankCount = 0;
  blankLabels: string[] = Array(MAX_BLANK_COLUMNS).fill('');

  private peopleById = new Map<number, People>();
  private previousTitle = '';

  constructor(
    private giftService: GiftService,
    private peopleService: PeopleService,
    private functionService: FunctionService,
    private translateService: TranslateService
  ) {}

  ngOnInit(): void {
    this.previousTitle = document.title;
    document.title = this.translateService.translate('report.giftTitle')
      + ' - ' + this.generatedOn.toISOString().slice(0, 10);
    forkJoin({
      fns: this.functionService.getAllFunctions(),
      people: this.peopleService.getAllPeople(),
      gifts: this.giftService.getAll()
    }).subscribe({
      next: ({ fns, people, gifts }) => {
        this.peopleById = new Map(people.filter(p => p.id != null).map(p => [p.id!, p]));
        this.gifts = gifts;
        this.sections = fns
          .map(fn => ({
            fn,
            gifts: gifts
              .filter(g => g.functionId === fn.id)
              .sort((a, b) => this.guestName(a).localeCompare(this.guestName(b)))
          }))
          .filter(section => section.gifts.length > 0);
        this.isLoading = false;
      },
      error: () => { this.isLoading = false; }
    });
  }

  ngOnDestroy(): void {
    document.title = this.previousTitle;
  }

  get visibleSections(): FunctionGifts[] {
    if (this.selectedFunctionId === 'ALL') { return this.sections; }
    return this.sections.filter(s => s.fn.id === this.selectedFunctionId);
  }

  guestName(gift: Gift): string {
    return this.peopleById.get(gift.personId)?.name ?? '—';
  }

  guestCity(gift: Gift): string {
    return this.peopleById.get(gift.personId)?.city ?? '';
  }

  sectionValueTotal(section: FunctionGifts): number {
    return section.gifts.reduce((sum, g) => sum + (g.value ?? 0), 0);
  }

  grandValueTotal(): number {
    return this.visibleSections.reduce((sum, s) => sum + this.sectionValueTotal(s), 0);
  }

  grandGiftCount(): number {
    return this.visibleSections.reduce((sum, s) => sum + s.gifts.length, 0);
  }

  onBlankCountChange(): void {
    const n = Math.floor(Number(this.blankCount) || 0);
    this.blankCount = Math.min(Math.max(n, 0), MAX_BLANK_COLUMNS);
  }

  blankColumns(): string[] {
    return this.blankLabels.slice(0, this.blankCount);
  }

  trackByIndex(index: number): number {
    return index;
  }

  print(): void {
    window.print();
  }
}
