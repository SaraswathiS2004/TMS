import { Component, OnDestroy, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { forkJoin } from 'rxjs';
import { PeopleService } from '../../services/people.service';
import { FunctionService } from '../../services/function.service';
import { People } from '../../models/people.model';
import { TmsFunction } from '../../models/function.model';
import { TranslatePipe } from '../../pipes/translate.pipe';
import { TranslateService } from '../../services/translate.service';

interface CityPage {
  city: string;
  people: People[];
}

const MAX_BLANK_COLUMNS = 5;

@Component({
  selector: 'app-guest-city-report',
  standalone: true,
  imports: [CommonModule, FormsModule, TranslatePipe],
  templateUrl: './guest-city-report.component.html',
  styleUrl: './guest-city-report.component.css'
})
export class GuestCityReportComponent implements OnInit, OnDestroy {

  cityPages: CityPage[] = [];
  functions: TmsFunction[] = [];
  isLoading = true;
  generatedOn = new Date();

  // Field selection (name and # are always shown)
  showMembers = true;
  showRelation = true;
  showGroup = true;
  showFunctions = true;

  // Blank pen-fillable columns
  readonly maxBlankColumns = MAX_BLANK_COLUMNS;
  blankCount = 0;
  blankLabels: string[] = Array(MAX_BLANK_COLUMNS).fill('');

  private previousTitle = '';

  constructor(
    private peopleService: PeopleService,
    private functionService: FunctionService,
    private translateService: TranslateService
  ) {}

  ngOnInit(): void {
    this.previousTitle = document.title;
    document.title = this.translateService.translate('report.guestCityTitle')
      + ' - ' + this.generatedOn.toISOString().slice(0, 10);
    forkJoin({
      fns: this.functionService.getAllFunctions(),
      people: this.peopleService.getAllPeople()
    }).subscribe({
      next: ({ fns, people }) => {
        this.functions = fns;
        this.cityPages = this.buildPages(people);
        this.isLoading = false;
      },
      error: () => { this.isLoading = false; }
    });
  }

  ngOnDestroy(): void {
    document.title = this.previousTitle;
  }

  private buildPages(people: People[]): CityPage[] {
    const map = new Map<string, People[]>();
    for (const p of people) {
      const key = p.city || this.translateService.translate('common.unknown');
      if (!map.has(key)) { map.set(key, []); }
      map.get(key)!.push(p);
    }
    return Array.from(map.entries())
      .map(([city, members]) => ({
        city,
        people: [...members].sort((a, b) => a.name.localeCompare(b.name))
      }))
      .sort((a, b) => a.city.localeCompare(b.city));
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

  totalExpected(page: CityPage): number {
    return page.people.reduce((sum, p) => sum + p.numberOfPerson, 0);
  }

  notInvitedCount(page: CityPage): number {
    return page.people.filter(p => p.invitedFunctionIds.length === 0).length;
  }

  isInvited(person: People, fnId: number): boolean {
    return person.invitedFunctionIds.includes(fnId);
  }

  print(): void {
    window.print();
  }
}
