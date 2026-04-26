import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { PeopleService } from '../../services/people.service';
import { People } from '../../models/people.model';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './dashboard.component.html',
  styleUrl: './dashboard.component.css'
})
export class DashboardComponent implements OnInit {

  people: People[] = [];
  isLoading = true;

  constructor(private peopleService: PeopleService) {}

  ngOnInit(): void {
    this.peopleService.getAllPeople().subscribe({
      next: (data) => {
        this.people = data;
        this.isLoading = false;
      },
      error: () => {
        this.isLoading = false;
      }
    });
  }

  // Total number of people in the list
  get totalPeople(): number {
    return this.people.length;
  }

  // People who have not been invited yet
  get notInvitedCount(): number {
    return this.people.filter(p => p.invitedStatus === 'NOT_INVITED').length;
  }

  // People invited only to the engagement
  get engagementInvited(): People[] {
    return this.people.filter(p => p.invitedStatus === 'ENGAGEMENT_INVITED');
  }

  // People invited only to the marriage
  get marriageInvited(): People[] {
    return this.people.filter(p => p.invitedStatus === 'MARRIAGE_INVITED');
  }

  // People invited to both events
  get bothInvited(): People[] {
    return this.people.filter(p => p.invitedStatus === 'BOTH_INVITED');
  }

  // Total attendees expected at the engagement
  get engagementExpectedCount(): number {
    return this.engagementInvited.reduce((total, p) => total + p.numberOfPerson, 0);
  }

  // Total attendees expected at the marriage
  get marriageExpectedCount(): number {
    return this.marriageInvited.reduce((total, p) => total + p.numberOfPerson, 0);
  }

  // Total attendees expected for both events combined
  get bothExpectedCount(): number {
    return this.bothInvited.reduce((total, p) => total + p.numberOfPerson, 0);
  }
}
