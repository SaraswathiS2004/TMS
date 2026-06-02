import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { PeopleService } from '../../services/people.service';
import { FunctionService } from '../../services/function.service';
import { GuestGroupService } from '../../services/guest-group.service';
import { People } from '../../models/people.model';
import { TmsFunction } from '../../models/function.model';
import { GuestGroup } from '../../models/guest-group.model';
import { TranslatePipe } from '../../pipes/translate.pipe';
import { TranslateService } from '../../services/translate.service';

interface GroupBucket {
  id: number | null;   // null = No Group
  name: string;
  color: string;
  people: People[];
  expanded: boolean;
}

@Component({
  selector: 'app-by-group',
  standalone: true,
  imports: [CommonModule, TranslatePipe],
  templateUrl: './by-group.component.html',
  styleUrl: './by-group.component.css'
})
export class ByGroupComponent implements OnInit {

  buckets: GroupBucket[] = [];
  functions: TmsFunction[] = [];
  isLoading = true;

  private readonly noGroupColor = '#94a3b8';

  constructor(
    private peopleService: PeopleService,
    private functionService: FunctionService,
    private guestGroupService: GuestGroupService,
    private translateService: TranslateService
  ) {}

  ngOnInit(): void {
    this.functionService.getAllFunctions().subscribe({
      next: (fns) => {
        this.functions = fns;
        this.guestGroupService.getAllGroups().subscribe({
          next: (groups) => {
            this.peopleService.getAllPeople().subscribe({
              next: (people) => {
                this.buckets = this.buildBuckets(groups, people);
                this.isLoading = false;
              },
              error: () => { this.isLoading = false; }
            });
          },
          error: () => { this.isLoading = false; }
        });
      },
      error: () => { this.isLoading = false; }
    });
  }

  private buildBuckets(groups: GuestGroup[], people: People[]): GroupBucket[] {
    const byId = new Map<number, GroupBucket>();
    const result: GroupBucket[] = [];
    for (const g of groups) {
      const bucket: GroupBucket = { id: g.id, name: g.name, color: g.color, people: [], expanded: true };
      byId.set(g.id, bucket);
      result.push(bucket);
    }
    const noGroup: GroupBucket = {
      id: null,
      name: this.translateService.translate('byGroup.noGroup'),
      color: this.noGroupColor,
      people: [],
      expanded: true
    };
    for (const p of people) {
      const bucket = p.groupId != null ? byId.get(p.groupId) : undefined;
      (bucket ?? noGroup).people.push(p);
    }
    for (const b of result) {
      b.people.sort((a, c) => a.name.localeCompare(c.name));
    }
    noGroup.people.sort((a, c) => a.name.localeCompare(c.name));
    if (noGroup.people.length > 0) {
      result.push(noGroup);
    }
    return result;
  }

  toggle(bucket: GroupBucket): void {
    bucket.expanded = !bucket.expanded;
  }

  totalExpected(bucket: GroupBucket): number {
    return bucket.people.reduce((sum, p) => sum + p.numberOfPerson, 0);
  }

  notInvitedCount(bucket: GroupBucket): number {
    return bucket.people.filter(p => p.invitedFunctionIds.length === 0).length;
  }

  countByFunction(bucket: GroupBucket, fnId: number): number {
    return bucket.people.filter(p => p.invitedFunctionIds.includes(fnId)).length;
  }

  functionColor(id: number): string {
    return this.functions.find(f => f.id === id)?.color ?? '#94a3b8';
  }

  relationBadgeClass(relation: string): string {
    const map: Record<string, string> = {
      CLOSE_RELATIVE: 'badge-close', DISTANCE_RELATIVE: 'badge-distance', FRIENDS: 'badge-friends'
    };
    return map[relation] ?? '';
  }
}
