import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FunctionService } from '../../services/function.service';
import { PeopleService } from '../../services/people.service';
import { TmsFunction } from '../../models/function.model';
import { People, effectivePersonCount } from '../../models/people.model';
import { TranslatePipe } from '../../pipes/translate.pipe';

interface FunctionStat {
  fn: TmsFunction;
  total: number;        // invitees added to this function
  invited: number;      // of those, already marked INVITED
  yetToInvite: number;  // of those, still to invite
  expectedCount: number;
}

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule, TranslatePipe],
  templateUrl: './dashboard.component.html',
  styleUrl: './dashboard.component.css'
})
export class DashboardComponent implements OnInit {

  people: People[] = [];
  functions: TmsFunction[] = [];
  functionStats: FunctionStat[] = [];
  isLoading = true;

  constructor(
    private functionService: FunctionService,
    private peopleService: PeopleService
  ) {}

  ngOnInit(): void {
    this.functionService.getAllFunctions().subscribe({
      next: (fns) => {
        this.functions = fns;
        this.peopleService.getAllPeople().subscribe({
          next: (people) => {
            this.people = people;
            this.functionStats = fns.map(fn => {
              const inFunction = people.filter(p => p.invitedFunctionIds.includes(fn.id));
              const invited = inFunction.filter(p => p.functionStatuses?.[String(fn.id)] === 'INVITED').length;
              return {
                fn,
                total: inFunction.length,
                invited,
                yetToInvite: inFunction.length - invited,
                expectedCount: inFunction.reduce((sum, p) => sum + effectivePersonCount(p), 0)
              };
            });
            this.isLoading = false;
          },
          error: () => { this.isLoading = false; }
        });
      },
      error: () => { this.isLoading = false; }
    });
  }

  get totalPeople(): number {
    return this.people.length;
  }

  get totalInvited(): number {
    return this.people.filter(p =>
      Object.values(p.functionStatuses ?? {}).some(s => s === 'INVITED')
    ).length;
  }

  get totalExpected(): number {
    return this.people.reduce((sum, p) => sum + effectivePersonCount(p), 0);
  }

  invitedPercent(stat: FunctionStat): number {
    return stat.total === 0 ? 0 : Math.round((stat.invited / stat.total) * 100);
  }
}
