import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { PeopleService } from '../../services/people.service';
import { People, InvitedStatus } from '../../models/people.model';

// Describes each filter tab shown at the top of the list
interface FilterTab {
  label: string;
  value: InvitedStatus | 'ALL';
}

@Component({
  selector: 'app-people-list',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './people-list.component.html',
  styleUrl: './people-list.component.css'
})
export class PeopleListComponent implements OnInit {

  people: People[] = [];
  isLoading = true;

  // The active filter – default is "Not Invited" so you see who still needs an invite
  activeFilter: InvitedStatus | 'ALL' = 'NOT_INVITED';

  // Track which person's "Mark Invited" popup is open (by their ID)
  openPopupId: number | null = null;

  // Feedback message shown briefly after marking someone as invited
  feedbackMessage = '';

  // All available filter tabs
  filterTabs: FilterTab[] = [
    { label: 'All',           value: 'ALL' },
    { label: 'Yet to Invite', value: 'NOT_INVITED' },
    { label: 'Engagement',    value: 'ENGAGEMENT_INVITED' },
    { label: 'Marriage',      value: 'MARRIAGE_INVITED' },
    { label: 'Both',          value: 'BOTH_INVITED' }
  ];

  constructor(private peopleService: PeopleService) {}

  ngOnInit(): void {
    this.loadPeople();
  }

  // Fetches people based on the current active filter
  loadPeople(): void {
    this.isLoading = true;
    this.openPopupId = null;

    const request$ = this.activeFilter === 'ALL'
      ? this.peopleService.getAllPeople()
      : this.peopleService.getPeopleByStatus(this.activeFilter);

    request$.subscribe({
      next: (data) => {
        this.people = data;
        this.isLoading = false;
      },
      error: () => {
        this.isLoading = false;
      }
    });
  }

  // Called when a filter tab is clicked
  selectFilter(filter: InvitedStatus | 'ALL'): void {
    this.activeFilter = filter;
    this.loadPeople();
  }

  // Toggles the "Mark Invited" popup for a specific person
  togglePopup(personId: number): void {
    this.openPopupId = this.openPopupId === personId ? null : personId;
  }

  // Sends the update request and refreshes the list
  markAsInvited(person: People, newStatus: InvitedStatus): void {
    if (!person.id) { return; }

    this.peopleService.updateInvitedStatus(person.id, newStatus).subscribe({
      next: (response) => {
        this.feedbackMessage = `${person.name} marked as ${this.formatStatus(newStatus)}.`;
        this.loadPeople();
        setTimeout(() => { this.feedbackMessage = ''; }, 3000);
      },
      error: () => {
        this.feedbackMessage = 'Update failed. Please try again.';
        setTimeout(() => { this.feedbackMessage = ''; }, 3000);
      }
    });
  }

  // Returns human-readable label for an invitation status
  formatStatus(status: InvitedStatus | string): string {
    const labels: Record<string, string> = {
      NOT_INVITED:         'Not Invited',
      ENGAGEMENT_INVITED:  'Engagement Invited',
      MARRIAGE_INVITED:    'Marriage Invited',
      BOTH_INVITED:        'Both Invited'
    };
    return labels[status] ?? status;
  }

  // Returns the CSS class for a status badge
  statusBadgeClass(status: InvitedStatus): string {
    const classes: Record<InvitedStatus, string> = {
      NOT_INVITED:        'status-not-invited',
      ENGAGEMENT_INVITED: 'status-engagement',
      MARRIAGE_INVITED:   'status-marriage',
      BOTH_INVITED:       'status-both'
    };
    return classes[status];
  }

  // Returns the CSS class for a relation type badge
  relationBadgeClass(relation: string): string {
    const classes: Record<string, string> = {
      CLOSE:    'badge-close',
      DISTANCE: 'badge-distance',
      FRIENDS:  'badge-friends'
    };
    return classes[relation] ?? '';
  }
}
