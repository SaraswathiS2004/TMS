import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FunctionService } from '../../services/function.service';
import { PeopleService } from '../../services/people.service';
import { GiftService } from '../../services/gift.service';
import { TmsFunction } from '../../models/function.model';
import { People } from '../../models/people.model';
import { Gift } from '../../models/gift.model';
import { TranslatePipe } from '../../pipes/translate.pipe';

interface FunctionStat {
  fn: TmsFunction;
  total: number;        // invitees added to this function
  invited: number;      // of those, already marked INVITED
  yetToInvite: number;  // of those, still to invite
  expectedCount: number;
  giftCount: number;    // gifts received for this function
  giftValue: number;    // total ₹ value of those gifts
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
  gifts: Gift[] = [];
  isLoading = true;

  constructor(
    private functionService: FunctionService,
    private peopleService: PeopleService,
    private giftService: GiftService
  ) {}

  ngOnInit(): void {
    this.giftService.getAll().subscribe({
      next: (gifts) => { this.gifts = gifts; this.buildStats(); }
    });

    this.functionService.getAllFunctions().subscribe({
      next: (fns) => {
        this.functions = fns;
        this.peopleService.getAllPeople().subscribe({
          next: (people) => {
            this.people = people;
            this.buildStats();
            this.isLoading = false;
          },
          error: () => { this.isLoading = false; }
        });
      },
      error: () => { this.isLoading = false; }
    });
  }

  private buildStats(): void {
    this.functionStats = this.functions.map(fn => {
      const inFunction = this.people.filter(p => p.invitedFunctionIds.includes(fn.id));
      const invited = inFunction.filter(p => p.functionStatuses?.[String(fn.id)] === 'INVITED').length;
      const fnGifts = this.gifts.filter(g => g.functionId === fn.id);
      return {
        fn,
        total: inFunction.length,
        invited,
        yetToInvite: inFunction.length - invited,
        expectedCount: inFunction.reduce((sum, p) => sum + p.numberOfPerson, 0),
        giftCount: fnGifts.length,
        giftValue: fnGifts.reduce((sum, g) => sum + (g.value ?? 0), 0)
      };
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
    return this.people.reduce((sum, p) => sum + p.numberOfPerson, 0);
  }

  invitedPercent(stat: FunctionStat): number {
    return stat.total === 0 ? 0 : Math.round((stat.invited / stat.total) * 100);
  }

  get totalGiftValue(): number {
    return this.gifts.reduce((sum, g) => sum + (g.value ?? 0), 0);
  }

  get totalGiftCount(): number {
    return this.gifts.length;
  }

  get uncostedGiftCount(): number {
    return this.gifts.filter(g => g.value == null).length;
  }
}
