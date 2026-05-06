import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FunctionService } from '../../services/function.service';
import { PeopleService } from '../../services/people.service';
import { TmsFunction } from '../../models/function.model';
import { People } from '../../models/people.model';
import { TranslatePipe } from '../../pipes/translate.pipe';

interface FunctionStat {
  fn: TmsFunction;
  inviteeCount: number;
  pendingCount: number;
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
              const invited = people.filter(p => p.invitedFunctionIds.includes(fn.id));
              return {
                fn,
                inviteeCount: invited.length,
                pendingCount: people.length - invited.length,
                expectedCount: invited.reduce((sum, p) => sum + p.numberOfPerson, 0)
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
    return this.people.filter(p => p.invitedFunctionIds.length > 0).length;
  }

  get totalExpected(): number {
    return this.people.reduce((sum, p) => sum + p.numberOfPerson, 0);
  }
}
