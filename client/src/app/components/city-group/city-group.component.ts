import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { PeopleService } from '../../services/people.service';
import { FunctionService } from '../../services/function.service';
import { People } from '../../models/people.model';
import { TmsFunction } from '../../models/function.model';
import { TranslatePipe } from '../../pipes/translate.pipe';

interface CityGroup {
  city: string;
  people: People[];
  expanded: boolean;
}

@Component({
  selector: 'app-city-group',
  standalone: true,
  imports: [CommonModule, TranslatePipe],
  templateUrl: './city-group.component.html',
  styleUrl: './city-group.component.css'
})
export class CityGroupComponent implements OnInit {

  cityGroups: CityGroup[] = [];
  functions: TmsFunction[] = [];
  isLoading = true;

  constructor(
    private peopleService: PeopleService,
    private functionService: FunctionService
  ) {}

  ngOnInit(): void {
    this.functionService.getAllFunctions().subscribe({
      next: (fns) => {
        this.functions = fns;
        this.peopleService.getAllPeople().subscribe({
          next: (people) => {
            this.cityGroups = this.buildGroups(people);
            this.isLoading = false;
          },
          error: () => { this.isLoading = false; }
        });
      },
      error: () => { this.isLoading = false; }
    });
  }

  private buildGroups(people: People[]): CityGroup[] {
    const map = new Map<string, People[]>();
    for (const p of people) {
      const key = p.city || 'Unknown';
      if (!map.has(key)) { map.set(key, []); }
      map.get(key)!.push(p);
    }
    return Array.from(map.entries())
      .map(([city, members]) => ({ city, people: members, expanded: true }))
      .sort((a, b) => a.city.localeCompare(b.city));
  }

  toggleCity(group: CityGroup): void {
    group.expanded = !group.expanded;
  }

  totalExpected(group: CityGroup): number {
    return group.people.reduce((sum, p) => sum + p.numberOfPerson, 0);
  }

  notInvitedCount(group: CityGroup): number {
    return group.people.filter(p => p.invitedFunctionIds.length === 0).length;
  }

  countByFunction(group: CityGroup, fnId: number): number {
    return group.people.filter(p => p.invitedFunctionIds.includes(fnId)).length;
  }

  functionColor(id: number): string {
    return this.functions.find(f => f.id === id)?.color ?? '#94a3b8';
  }

  relationBadgeClass(relation: string): string {
    const map: Record<string, string> = {
      CLOSE: 'badge-close', DISTANCE: 'badge-distance', FRIENDS: 'badge-friends'
    };
    return map[relation] ?? '';
  }
}
